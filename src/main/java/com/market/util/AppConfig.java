package com.market.util;

import java.io.InputStream;
import java.util.Properties;
import jakarta.servlet.ServletContext;

/** WEB-INF/app.properties 를 읽고, 같은 이름의 환경변수(점→밑줄, 대문자)가 있으면 우선 사용합니다. */
public final class AppConfig {
    private static final Properties P = new Properties();

    private AppConfig() {}

    public static synchronized void load(ServletContext ctx) {
        try (InputStream in = ctx.getResourceAsStream("/WEB-INF/app.properties")) {
            if (in != null) P.load(new java.io.InputStreamReader(in, "UTF-8"));
        } catch (Exception e) {
            ctx.log("app.properties 를 읽지 못했습니다: " + e);
        }
    }

    public static String get(String key, String def) {
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env.trim();
        String v = P.getProperty(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }
}
