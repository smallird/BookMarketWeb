package com.market.coupon;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.market.util.Db;

public final class CouponDao {
    private CouponDao() {}

    private static final String COLS = "code,name,discount_type,discount_value,min_order,max_discount,expires_at,active,auto_issue";

    static Coupon map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("expires_at");
        return new Coupon(rs.getString("code"), rs.getString("name"), rs.getString("discount_type"),
                rs.getInt("discount_value"), rs.getInt("min_order"), rs.getInt("max_discount"),
                d == null ? null : d.toLocalDate(), rs.getInt("active") == 1, rs.getInt("auto_issue") == 1);
    }

    /** 처음 실행할 때 보여줄 기본 쿠폰 */
    public static void seedDefaults(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM coupons")) {
            rs.next(); if (rs.getInt(1) > 0) return;
        }
        create(c, new Coupon("WELCOME10", "신규 가입 10% 쿠폰", Coupon.PERCENT, 10, 10000, 5000, null, true, true));
        create(c, new Coupon("BOOK3000", "독서의 달 3,000원 쿠폰", Coupon.AMOUNT, 3000, 20000, 0, null, true, false));
    }

    public static void create(Connection c, Coupon k) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO coupons (" + COLS + ") VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, k.getCode()); ps.setString(2, k.getName()); ps.setString(3, k.getType());
            ps.setInt(4, k.getValue()); ps.setInt(5, k.getMinOrder()); ps.setInt(6, k.getMaxDiscount());
            if (k.getExpiresAt() == null) ps.setNull(7, Types.DATE); else ps.setDate(7, Date.valueOf(k.getExpiresAt()));
            ps.setInt(8, k.isActive() ? 1 : 0); ps.setInt(9, k.isAutoIssue() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public static boolean create(Coupon k) throws SQLException {
        try (Connection c = Db.get()) {
            if (find(c, k.getCode()) != null) return false;
            create(c, k);
            return true;
        }
    }

    public static Coupon find(Connection c, String code) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM coupons WHERE code=?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public static List<Coupon> listAll() throws SQLException {
        List<Coupon> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLS + " FROM coupons ORDER BY created_at DESC, code")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public static void setActive(String code, boolean active) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("UPDATE coupons SET active=? WHERE code=?")) {
            ps.setInt(1, active ? 1 : 0); ps.setString(2, code); ps.executeUpdate();
        }
    }

    public static void delete(String code) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement a = c.prepareStatement("DELETE FROM user_coupons WHERE coupon_code=? AND used_at IS NULL");
                 PreparedStatement b = c.prepareStatement("DELETE FROM coupons WHERE code=?")) {
                a.setString(1, code); a.executeUpdate();
                b.setString(1, code); b.executeUpdate();
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    /** 가입 시 자동으로 나눠주는 쿠폰 지급 (호출하는 쪽의 트랜잭션 안에서 실행) */
    public static void issueAutoCoupons(Connection c, String userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO user_coupons (user_id, coupon_code) SELECT ?, code FROM coupons WHERE auto_issue=1 AND active=1")) {
            ps.setString(1, userId); ps.executeUpdate();
        }
    }

    public enum RegisterResult { OK, NOT_FOUND, INACTIVE, EXPIRED, ALREADY }

    /** 회원이 쿠폰 코드를 입력해 등록 */
    public static RegisterResult register(String userId, String code) throws SQLException {
        try (Connection c = Db.get()) {
            Coupon k = find(c, code);
            if (k == null) return RegisterResult.NOT_FOUND;
            if (!k.isActive()) return RegisterResult.INACTIVE;
            if (k.isExpired()) return RegisterResult.EXPIRED;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM user_coupons WHERE user_id=? AND coupon_code=?")) {
                ps.setString(1, userId); ps.setString(2, k.getCode());
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return RegisterResult.ALREADY; }
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO user_coupons (user_id, coupon_code) VALUES (?,?)")) {
                ps.setString(1, userId); ps.setString(2, k.getCode()); ps.executeUpdate();
            } catch (SQLException dup) { return RegisterResult.ALREADY; }   // 동시에 두 번 눌렀을 때
            return RegisterResult.OK;
        }
    }

    public static List<UserCoupon> listForUser(String userId) throws SQLException {
        List<UserCoupon> list = new ArrayList<>();
        String sql = "SELECT k.code,k.name,k.discount_type,k.discount_value,k.min_order,k.max_discount,k.expires_at,k.active,k.auto_issue,u.used_at"
                + " FROM user_coupons u JOIN coupons k ON k.code=u.coupon_code WHERE u.user_id=? ORDER BY u.issued_at DESC";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new UserCoupon(map(rs), rs.getTimestamp("used_at")));
            }
        }
        return list;
    }

    /** 주문 시 검증: 내가 가진, 아직 안 쓴, 유효한 쿠폰이면 반환 (아니면 null) */
    public static Coupon findUsable(Connection c, String userId, String code) throws SQLException {
        String sql = "SELECT k.code,k.name,k.discount_type,k.discount_value,k.min_order,k.max_discount,k.expires_at,k.active,k.auto_issue"
                + " FROM user_coupons u JOIN coupons k ON k.code=u.coupon_code WHERE u.user_id=? AND u.coupon_code=? AND u.used_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId); ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Coupon k = map(rs);
                return (k.isActive() && !k.isExpired()) ? k : null;
            }
        }
    }

    /** 사용 처리. 이미 쓴 쿠폰이면 false (이중 사용 방지) */
    public static boolean markUsed(Connection c, String userId, String code, String orderNo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE user_coupons SET used_at=?, order_no=? WHERE user_id=? AND coupon_code=? AND used_at IS NULL")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString(2, orderNo); ps.setString(3, userId); ps.setString(4, code);
            return ps.executeUpdate() == 1;
        }
    }
}
