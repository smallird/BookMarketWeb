package com.market.member;

import jakarta.servlet.http.*;

/** 세션 로그인 / 권한 확인 도우미. */
public final class Auth {
    private Auth() {}

    public static void login(HttpServletRequest req, Member m, String type) {
        HttpSession s = req.getSession();
        req.changeSessionId();                       // 세션 고정 공격 방지 (장바구니 등 기존 값은 유지)
        s.setAttribute("userId", m.getId());
        s.setAttribute("memberName", m.getName());
        s.setAttribute("memberPhone", m.getPhone() == null ? "" : m.getPhone());
        s.setAttribute("memberAddress", m.getAddress() == null ? "" : m.getAddress());
        s.setAttribute("role", m.getRole());
        s.setAttribute("genrePreferences", m.getGenrePreferences());
        s.setAttribute("interestKeywords", m.getInterestKeywords());
        s.setAttribute("loginType", type);
        try { com.market.cart.CartDao.mergeOnLogin(s, m.getId()); }   // 저장된 장바구니 불러오기
        catch (Exception e) { s.getServletContext().log("장바구니를 불러오지 못했습니다", e); }
    }

    public static boolean isAdmin(HttpSession s) {
        return s != null && "관리자".equals(s.getAttribute("role"));
    }

    /** 관리자가 아니면 로그인 화면으로 보내고 true 를 돌려줍니다. */
    public static boolean denyUnlessAdmin(HttpServletRequest req, HttpServletResponse res) throws java.io.IOException {
        if (isAdmin(req.getSession(false))) return false;
        res.sendRedirect(req.getContextPath() + "/login.jsp?error=admin");
        return true;
    }

    public static void flash(HttpSession s, String type, String msg) {
        s.setAttribute("flashType", type);
        s.setAttribute("flashMsg", msg);
    }
}
