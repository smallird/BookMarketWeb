package com.market.util;

import java.util.regex.*;

/** 카카오 응답에서 값 하나만 꺼내 쓰기 위한 최소한의 JSON 도우미 (외부 라이브러리 없이). */
public final class Json {
    private Json() {}

    public static String str(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        return m.find() ? unescape(m.group(1)) : null;
    }

    public static Long num(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? Long.valueOf(m.group(1)) : null;
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) { sb.append(c); continue; }
            char n = s.charAt(++i);
            switch (n) {
                case 'n': sb.append('\n'); break;
                case 't': sb.append('\t'); break;
                case 'u': sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16)); i += 4; break;
                default: sb.append(n);
            }
        }
        return sb.toString();
    }
}
