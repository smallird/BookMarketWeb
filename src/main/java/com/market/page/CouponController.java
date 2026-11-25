package com.market.page;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.market.coupon.*;
import com.market.member.Auth;

/** 회원용: 쿠폰 코드 등록 / 관리자용: 쿠폰 만들기·중지·삭제 */
@WebServlet({"/coupon.do", "/adminCoupon.do"})
public class CouponController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (req.getServletPath().equals("/adminCoupon.do")) admin(req, res); else member(req, res);
    }

    private void member(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession s = req.getSession();
        String userId = (String) s.getAttribute("userId");
        String ctx = req.getContextPath();
        if (userId == null) { res.sendRedirect(ctx + "/login.jsp"); return; }
        String code = req.getParameter("code") == null ? "" : req.getParameter("code").trim().toUpperCase();
        try {
            switch (CouponDao.register(userId, code)) {
                case OK:         Auth.flash(s, "success", "쿠폰이 등록되었어요."); break;
                case NOT_FOUND:  Auth.flash(s, "danger", "없는 쿠폰 코드입니다. 코드를 다시 확인해 주세요."); break;
                case INACTIVE:   Auth.flash(s, "danger", "지금은 사용할 수 없는 쿠폰입니다."); break;
                case EXPIRED:    Auth.flash(s, "danger", "기한이 지난 쿠폰입니다."); break;
                case ALREADY:    Auth.flash(s, "warning", "이미 등록한 쿠폰입니다."); break;
            }
        } catch (SQLException e) { log("coupon register", e); Auth.flash(s, "danger", "쿠폰을 등록하지 못했습니다. 잠시 후 다시 시도해 주세요."); }
        res.sendRedirect(ctx + "/coupon.jsp");
    }

    private void admin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (Auth.denyUnlessAdmin(req, res)) return;
        HttpSession s = req.getSession();
        String action = req.getParameter("action");
        String code = req.getParameter("code") == null ? "" : req.getParameter("code").trim().toUpperCase();
        try {
            if ("create".equals(action)) {
                if (!code.matches("[A-Z0-9_-]{3,30}")) { Auth.flash(s, "danger", "쿠폰 코드는 영문 대문자·숫자·-·_ 3~30자로 입력해 주세요."); }
                else {
                    String type = Coupon.PERCENT.equals(req.getParameter("type")) ? Coupon.PERCENT : Coupon.AMOUNT;
                    int value = num(req.getParameter("value")), min = num(req.getParameter("minOrder")), max = num(req.getParameter("maxDiscount"));
                    String exp = req.getParameter("expiresAt");
                    LocalDate expires = (exp == null || exp.isBlank()) ? null : LocalDate.parse(exp);
                    String name = req.getParameter("name") == null ? "" : req.getParameter("name").trim();
                    if (name.isEmpty() || value <= 0 || (Coupon.PERCENT.equals(type) && value > 100)) {
                        Auth.flash(s, "danger", "쿠폰 이름과 할인 값을 확인해 주세요. (% 할인은 1~100)");
                    } else {
                        boolean ok = CouponDao.create(new Coupon(code, name, type, value, min, max, expires, true, "on".equals(req.getParameter("autoIssue"))));
                        Auth.flash(s, ok ? "success" : "warning", ok ? "쿠폰 " + code + " 을(를) 만들었어요." : "이미 있는 쿠폰 코드입니다.");
                    }
                }
            } else if ("toggle".equals(action)) {
                CouponDao.setActive(code, "1".equals(req.getParameter("active")));
            } else if ("delete".equals(action)) {
                CouponDao.delete(code);
                Auth.flash(s, "success", "쿠폰 " + code + " 을(를) 삭제했어요.");
            }
        } catch (Exception e) { log("admin coupon", e); Auth.flash(s, "danger", "처리하지 못했습니다: " + e.getMessage()); }
        res.sendRedirect(req.getContextPath() + "/couponAdmin.jsp");
    }

    private static int num(String s) { try { return Math.max(0, Integer.parseInt(s.trim())); } catch (Exception e) { return 0; } }
}
