package com.market.member;

import java.sql.*;
import com.market.util.Db;
import com.market.util.Passwords;

public final class MemberDao {
    private MemberDao() {}

    private static final String COLS = "id,pw_hash,name,phone,address,role,kakao_id,genre_preferences,interest_keywords";

    private static Member map(ResultSet rs) throws SQLException {
        return new Member(rs.getString("id"), rs.getString("name"), rs.getString("phone"),
                rs.getString("address"), rs.getString("role"), rs.getString("kakao_id"), rs.getString("pw_hash"), rs.getString("genre_preferences"), rs.getString("interest_keywords"));
    }

    public static void insert(Connection c, String id, String pw, String name, String phone, String address,
                              String role, String kakaoId) throws SQLException {
        String sql = "INSERT INTO members (" + COLS + ") VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, pw == null ? null : Passwords.hash(pw));
            ps.setString(3, name); ps.setString(4, phone); ps.setString(5, address);
            ps.setString(6, role); ps.setString(7, kakaoId); ps.setString(8, ""); ps.setString(9, "");
            ps.executeUpdate();
        }
    }

    public static Member findById(String id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM members WHERE id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public static Member findByKakaoId(String kakaoId) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM members WHERE kakao_id=?")) {
            ps.setString(1, kakaoId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    /** 아이디/비밀번호가 맞으면 회원을, 아니면 null. 카카오 전용 계정(비밀번호 없음)은 항상 null. */
    public static Member authenticate(String id, String pw) throws SQLException {
        Member m = findById(id);
        return (m != null && Passwords.verify(pw, m.getPwHash())) ? m : null;
    }

    /** @return 가입 성공 시 true, 이미 있는 아이디면 false */
    public static boolean register(String id, String pw, String name, String phone, String address,
                                   String role, String kakaoId) throws SQLException {
        return register(id,pw,name,phone,address,role,kakaoId,"","");
    }

    public static boolean register(String id, String pw, String name, String phone, String address,
                                   String role, String kakaoId, String genres, String keywords) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                if (exists(c, id)) { c.rollback(); return false; }
                insert(c, id, pw, name, phone, address, role, kakaoId);
                try (PreparedStatement pref = c.prepareStatement("UPDATE members SET genre_preferences=?, interest_keywords=? WHERE id=?")) {
                    pref.setString(1, genres == null ? "" : genres);
                    pref.setString(2, keywords == null ? "" : keywords);
                    pref.setString(3, id); pref.executeUpdate();
                }
                com.market.coupon.CouponDao.issueAutoCoupons(c, id);
                c.commit();
                return true;
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    public static void updatePreferences(String id, String genres, String keywords) throws SQLException {
        try (Connection c=Db.get(); PreparedStatement ps=c.prepareStatement(
                "UPDATE members SET genre_preferences=?, interest_keywords=? WHERE id=?")) {
            ps.setString(1, genres == null ? "" : genres);
            ps.setString(2, keywords == null ? "" : keywords);
            ps.setString(3,id); ps.executeUpdate();
        }
    }


    public enum ProfileUpdateResult { OK, NOT_FOUND, PROFILE_LIMIT, ID_LIMIT, ID_TAKEN, INVALID_ID }

    /** 기본 회원정보는 월 3회, 로그인 아이디는 마지막 변경 시점부터 30일 후 재변경 가능합니다. */
    public static ProfileUpdateResult updateProfile(String oldId, String newId, String name, String phone, String address) throws SQLException {
        if (newId == null || !newId.matches("[A-Za-z0-9_]{4,50}")) return ProfileUpdateResult.INVALID_ID;
        java.time.ZoneId zone = java.time.ZoneId.of("Asia/Seoul");
        String month = java.time.YearMonth.now(zone).toString();
        java.time.LocalDateTime now = java.time.LocalDateTime.now(zone);
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                int profileCount;
                String profileMonth;
                java.sql.Timestamp lastIdChange;
                try (PreparedStatement ps = c.prepareStatement("SELECT profile_change_month, profile_change_count, id_change_at FROM members WHERE id=? FOR UPDATE")) {
                    ps.setString(1, oldId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return ProfileUpdateResult.NOT_FOUND; }
                        profileMonth=rs.getString(1); profileCount=rs.getInt(2);
                        lastIdChange=rs.getTimestamp(3);
                    }
                }
                boolean profileChanged = !same(name, currentValue(c, oldId, "name")) || !same(phone, currentValue(c, oldId, "phone")) || !same(address, currentValue(c, oldId, "address"));
                boolean idChanged = !oldId.equals(newId);
                if (profileChanged && month.equals(profileMonth) && profileCount >= 3) { c.rollback(); return ProfileUpdateResult.PROFILE_LIMIT; }
                if (idChanged && lastIdChange != null && now.isBefore(lastIdChange.toLocalDateTime().plusDays(30))) { c.rollback(); return ProfileUpdateResult.ID_LIMIT; }
                if (idChanged && exists(c, newId)) { c.rollback(); return ProfileUpdateResult.ID_TAKEN; }

                String sql = "UPDATE members SET id=?, name=?, phone=?, address=?, "
                        + "profile_change_month=?, profile_change_count=?, id_change_at=? WHERE id=?";
                try (PreparedStatement ps=c.prepareStatement(sql)) {
                    ps.setString(1,newId); ps.setString(2,name); ps.setString(3,phone); ps.setString(4,address);
                    ps.setString(5, profileChanged ? month : profileMonth);
                    ps.setInt(6, profileChanged ? (month.equals(profileMonth) ? profileCount+1 : 1) : profileCount);
                    if (idChanged) ps.setTimestamp(7, java.sql.Timestamp.valueOf(now));
                    else if (lastIdChange != null) ps.setTimestamp(7, lastIdChange);
                    else ps.setNull(7, java.sql.Types.TIMESTAMP);
                    ps.setString(8,oldId); ps.executeUpdate();
                }
                if (idChanged) {
                    for (String table : new String[]{"user_coupons", "orders", "cart_items"}) {
                        String column = "user_coupons".equals(table) ? "user_id" : "user_id";
                        try (PreparedStatement ps=c.prepareStatement("UPDATE " + table + " SET " + column + "=? WHERE " + column + "=?")) {
                            ps.setString(1,newId); ps.setString(2,oldId); ps.executeUpdate();
                        }
                    }
                }
                c.commit(); return ProfileUpdateResult.OK;
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    private static String currentValue(Connection c, String id, String column) throws SQLException {
        if (!"name".equals(column) && !"phone".equals(column) && !"address".equals(column)) throw new IllegalArgumentException("column");
        try (PreparedStatement ps=c.prepareStatement("SELECT " + column + " FROM members WHERE id=?")) {
            ps.setString(1,id); try (ResultSet rs=ps.executeQuery()) { return rs.next()?rs.getString(1):null; }
        }
    }
    private static boolean same(String a, String b) { return (a==null?"":a.trim()).equals(b==null?"":b.trim()); }

    private static boolean exists(Connection c, String id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM members WHERE id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
