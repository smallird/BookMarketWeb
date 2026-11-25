package com.market.member;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/join.do")
public class JoinController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String id = trim(req.getParameter("id")), pw = req.getParameter("password"), pw2 = req.getParameter("password2"),
               name = trim(req.getParameter("name")), phone = trim(req.getParameter("phone")), addr = trim(req.getParameter("address"));
        String genres = String.join(",", req.getParameterValues("genres") == null ? new String[0] : req.getParameterValues("genres"));
        String keywords = trim(req.getParameter("keywords"));
        String ctx = req.getContextPath();

        if (!id.matches("[A-Za-z0-9_]{4,20}")) { res.sendRedirect(ctx + "/join.jsp?error=id"); return; }
        if (pw == null || pw.length() < 4)    { res.sendRedirect(ctx + "/join.jsp?error=pw"); return; }
        if (!pw.equals(pw2))                  { res.sendRedirect(ctx + "/join.jsp?error=pw2"); return; }
        if (name.isEmpty())                   { res.sendRedirect(ctx + "/join.jsp?error=name"); return; }
        try {
            // 관리자 계정은 화면에서 만들 수 없고, 항상 일반회원으로 가입됩니다.
            if (!MemberDao.register(id, pw, name, phone, addr, "일반회원", null, genres, keywords)) {
                res.sendRedirect(ctx + "/join.jsp?error=dup"); return;
            }
            Auth.login(req, MemberDao.findById(id), "local");
            Auth.flash(req.getSession(), "success", "가입을 환영합니다. 신규 가입 쿠폰이 지급되었어요.");
            res.sendRedirect(ctx + "/index.jsp");
        } catch (Exception e) {
            log("join failed", e);
            res.sendRedirect(ctx + "/join.jsp?error=db");
        }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
