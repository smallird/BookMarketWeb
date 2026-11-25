package com.market.member;

import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/preferences.do")
public class PreferencesController extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session=req.getSession(false);
        String ctx=req.getContextPath();
        if(session==null || session.getAttribute("userId")==null){res.sendRedirect(ctx+"/login.jsp");return;}
        String[] selected=req.getParameterValues("genres");
        String genres=selected==null?"":String.join(",",selected);
        String keywords=req.getParameter("keywords");
        if(keywords==null) keywords="";
        keywords=keywords.trim();
        if(keywords.length()>500) keywords=keywords.substring(0,500);
        try {
            MemberDao.updatePreferences((String)session.getAttribute("userId"),genres,keywords);
            session.setAttribute("genrePreferences",genres);
            session.setAttribute("interestKeywords",keywords);
            Auth.flash(session,"success","독서 취향을 저장했습니다. 맞춤 도서를 추천해 드릴게요.");
            res.sendRedirect(ctx+"/index.jsp");
        } catch(Exception e) {
            getServletContext().log("preference update failed",e);
            res.sendRedirect(ctx+"/preferences.jsp?error=db");
        }
    }
}
