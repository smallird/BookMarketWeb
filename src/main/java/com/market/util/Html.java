package com.market.util;

/** JSP 에서 사용자 입력을 화면에 찍을 때 쓰는 HTML 이스케이프 (XSS 방지). */
public final class Html {
    private Html() {}
    public static String esc(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
