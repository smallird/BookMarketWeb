package com.market.member;
import java.io.*; import jakarta.servlet.*; import jakarta.servlet.annotation.*; import jakarta.servlet.http.*;
@WebServlet("/logout.do")
public class LogoutController extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException{ req.getSession().invalidate(); res.sendRedirect(req.getContextPath()+"/index.jsp"); }
}
