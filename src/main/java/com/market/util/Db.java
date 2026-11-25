package com.market.util;

import java.sql.*;

/** 아주 단순한 JDBC 연결 도우미. 요청마다 연결을 열고 try-with-resources 로 닫습니다. */
public final class Db {
    private Db() {}

    public static Connection get() throws SQLException {
        String url = AppConfig.get("db.url", "");
        if (url.isEmpty()) throw new SQLException("db.url 이 설정되지 않았습니다. WEB-INF/app.properties 를 확인하세요.");
        loadDriver(url);
        return DriverManager.getConnection(url, AppConfig.get("db.user", ""), AppConfig.get("db.password", ""));
    }

    private static void loadDriver(String url) {
        String cls = null;
        if (url.startsWith("jdbc:postgresql:")) cls = "org.postgresql.Driver";   // Supabase = PostgreSQL
        if (cls == null) return;
        try { Class.forName(cls); } catch (ClassNotFoundException ignore) { /* 드라이버 jar 가 없으면 getConnection 에서 오류가 납니다 */ }
    }
}
