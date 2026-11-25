package com.market.util;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import com.market.member.MemberDao;
import com.market.coupon.CouponDao;

/** 서버가 시작될 때 설정을 읽고, 테이블을 만들고, 처음 한 번만 기본 데이터를 넣습니다. */
@WebListener
public class AppInit implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent e) {
        ServletContext ctx = e.getServletContext();
        AppConfig.load(ctx);
        try (Connection c = Db.get()) {
            Schema.create(c);
            seedMembers(c);
            seedBooks(c, ctx);
            CouponDao.seedDefaults(c);
            ctx.removeAttribute("dbError");
            ctx.log("[BS's Library] DB 준비 완료");
        } catch (Exception ex) {
            ctx.setAttribute("dbError", ex.getClass().getSimpleName() + ": " + ex.getMessage());
            ctx.log("[BS's Library] DB 초기화 실패 - WEB-INF/app.properties 와 JDBC 드라이버를 확인하세요", ex);
        }
    }

    private void seedMembers(Connection c) throws SQLException {
        if (count(c, "members") > 0) return;
        MemberDao.insert(c, "admin", "1234", "관리자", "010-0000-0000", "서울특별시", "관리자", null);
        MemberDao.insert(c, "user", "1234", "홍길동", "010-1234-5678", "서울특별시", "일반회원", null);
    }

    private void seedBooks(Connection c, ServletContext ctx) throws Exception {
        if (count(c, "books") > 0) return;
        String sql = "INSERT INTO books (book_id,name,unit_price,author,description,category,release_date,sort_no) VALUES (?,?,?,?,?,?,?,?)";
        try (InputStream in = ctx.getResourceAsStream("/book.txt");
             PreparedStatement ps = c.prepareStatement(sql)) {
            long n = 0;
            if (in != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String id;
                while ((id = r.readLine()) != null) {
                    if (!id.startsWith("ISBN")) continue;
                    String name = r.readLine(), price = r.readLine(), author = r.readLine(),
                           desc = r.readLine(), cat = r.readLine(), date = r.readLine();
                    if (name == null || price == null) break;
                    ps.setString(1, id.trim()); ps.setString(2, name.trim());
                    ps.setInt(3, Integer.parseInt(price.trim()));
                    ps.setString(4, trim(author)); ps.setString(5, trim(desc));
                    ps.setString(6, trim(cat)); ps.setString(7, trim(date)); ps.setLong(8, ++n);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }

    static int count(Connection c, String table) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next(); return rs.getInt(1);
        }
    }
}
