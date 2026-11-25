package com.market.member;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

/** web.xml 에서 /login.do 로 연결되어 있습니다. */
public class LoginController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String id = req.getParameter("id"), pw = req.getParameter("password");
        try {
            Member m = (id == null || pw == null) ? null : MemberDao.authenticate(id.trim(), pw);
            if (m == null) { res.sendRedirect(req.getContextPath() + "/login.jsp?error=fail"); return; }
            Auth.login(req, m, "local");
            String genres=m.getGenrePreferences();
            res.sendRedirect(req.getContextPath() + ((genres==null || genres.isBlank())
                    ? "/preferences.jsp" : "/index.jsp"));
        } catch (Exception e) {
            log("login failed", e);
            res.sendRedirect(req.getContextPath() + "/login.jsp?error=db");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendRedirect(req.getContextPath() + "/login.jsp");
    }
}
