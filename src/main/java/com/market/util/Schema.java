package com.market.util;

import java.sql.*;

/** 테이블 생성 (이미 있으면 건너뜀). Supabase(PostgreSQL)용. */
public final class Schema {
    private Schema() {}

    private static final String[] DDL = {
        "CREATE TABLE IF NOT EXISTS members ("
        + " id VARCHAR(50) PRIMARY KEY, pw_hash VARCHAR(255), name VARCHAR(100) NOT NULL,"
        + " phone VARCHAR(30), address VARCHAR(255), role VARCHAR(20) NOT NULL,"
        + " kakao_id VARCHAR(30), genre_preferences VARCHAR(500), interest_keywords VARCHAR(500), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

        "CREATE TABLE IF NOT EXISTS books ("
        + " book_id VARCHAR(50) PRIMARY KEY, name VARCHAR(255) NOT NULL, unit_price INT NOT NULL,"
        + " author VARCHAR(255), description VARCHAR(1000), category VARCHAR(100), release_date VARCHAR(30),"
        + " sort_no BIGINT)",

        "CREATE TABLE IF NOT EXISTS coupons ("
        + " code VARCHAR(30) PRIMARY KEY, name VARCHAR(100) NOT NULL, discount_type VARCHAR(10) NOT NULL,"
        + " discount_value INT NOT NULL, min_order INT DEFAULT 0, max_discount INT DEFAULT 0,"
        + " expires_at DATE, active INT DEFAULT 1, auto_issue INT DEFAULT 0,"
        + " created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

        "CREATE TABLE IF NOT EXISTS user_coupons ("
        + " user_id VARCHAR(50) NOT NULL, coupon_code VARCHAR(30) NOT NULL,"
        + " issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, used_at TIMESTAMP NULL, order_no VARCHAR(40),"
        + " PRIMARY KEY (user_id, coupon_code))",

        "CREATE TABLE IF NOT EXISTS orders ("
        + " order_no VARCHAR(40) PRIMARY KEY, user_id VARCHAR(50) NOT NULL, ordered_at TIMESTAMP NOT NULL,"
        + " name VARCHAR(100), phone VARCHAR(30), address VARCHAR(255),"
        + " subtotal INT NOT NULL, discount INT NOT NULL DEFAULT 0, total INT NOT NULL,"
        + " coupon_code VARCHAR(30), coupon_name VARCHAR(100))",

        "CREATE TABLE IF NOT EXISTS order_items ("
        + " order_no VARCHAR(40) NOT NULL, book_id VARCHAR(50) NOT NULL, book_name VARCHAR(255),"
        + " unit_price INT NOT NULL, quantity INT NOT NULL, PRIMARY KEY (order_no, book_id))",

        "CREATE TABLE IF NOT EXISTS cart_items ("
        + " user_id VARCHAR(50) NOT NULL, book_id VARCHAR(50) NOT NULL, quantity INT NOT NULL,"
        + " pos INT NOT NULL DEFAULT 0, PRIMARY KEY (user_id, book_id))"
    };

    private static final String[] TABLES = { "members", "books", "coupons", "user_coupons", "orders", "order_items", "cart_items" };

    public static void create(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            for (String s : DDL) st.execute(s);
            // Supabase 는 public 스키마의 테이블을 REST API(anon 키)로도 노출합니다.
            // RLS 를 켜고 정책을 만들지 않으면 API 로는 접근할 수 없고,
            // 이 앱이 쓰는 DB 소유자(postgres) 연결은 RLS 를 우회하므로 그대로 동작합니다.
            // 기존 설치 사용자도 취향 기능을 사용할 수 있도록 컬럼을 안전하게 추가합니다.
            st.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS genre_preferences VARCHAR(500)");
            st.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS interest_keywords VARCHAR(500)");
            // 월별 회원정보 변경 횟수 관리 (기존 회원은 이번 달 0회부터 시작)
            st.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS profile_change_month VARCHAR(7)");
            st.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS profile_change_count INT NOT NULL DEFAULT 0");
            // 아이디 재변경은 마지막 변경 시점부터 30일이 지나야 허용합니다.
            st.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS id_change_at TIMESTAMP NULL");
            // 이전 버전 호환을 위해 기존 월별 컬럼은 유지합니다.
            st.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS id_change_month VARCHAR(7)");
            st.execute("ALTER TABLE members ADD COLUMN IF NOT EXISTS id_change_count INT NOT NULL DEFAULT 0");
            for (String t : TABLES) st.execute("ALTER TABLE " + t + " ENABLE ROW LEVEL SECURITY");
        }
    }
}
