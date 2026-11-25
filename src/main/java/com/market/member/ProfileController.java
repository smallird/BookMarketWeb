package com.market.member;

import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/profile.do")
public class ProfileController extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String ctx=req.getContextPath();
        HttpSession session=req.getSession(false);
        if(session==null || session.getAttribute("userId")==null){res.sendRedirect(ctx+"/login.jsp");return;}
        String oldId=(String)session.getAttribute("userId");
        String newId=trim(req.getParameter("id"));
        String name=trim(req.getParameter("name"));
        String phone=trim(req.getParameter("phone"));
        String address=trim(req.getParameter("address"));
        if(name.isEmpty() || name.length()>100 || phone.length()>30 || address.length()>255){
            Auth.flash(session,"danger","입력값을 확인해 주세요. 이름은 필수이며 입력 길이를 초과할 수 없습니다.");
            res.sendRedirect(ctx+"/member.jsp");return;
        }
        try {
            MemberDao.ProfileUpdateResult result=MemberDao.updateProfile(oldId,newId,name,phone,address);
            switch(result){
                case OK:
                    session.setAttribute("userId",newId); session.setAttribute("memberName",name);
                    session.setAttribute("memberPhone",phone); session.setAttribute("memberAddress",address);
                    Auth.flash(session,"success","회원정보가 수정되었습니다."); break;
                case PROFILE_LIMIT: Auth.flash(session,"danger","전화번호·이름·주소 등 회원정보 변경은 한 달에 최대 3회입니다."); break;
                case ID_LIMIT: Auth.flash(session,"danger","아이디 변경은 마지막 변경일로부터 30일이 지나야 다시 가능합니다."); break;
                case ID_TAKEN: Auth.flash(session,"danger","이미 사용 중인 아이디입니다."); break;
                case INVALID_ID: Auth.flash(session,"danger","아이디는 영문·숫자·밑줄 4~50자로 입력해 주세요."); break;
                default: Auth.flash(session,"danger","회원정보를 찾을 수 없습니다. 다시 로그인해 주세요.");
            }
        } catch(Exception e){ getServletContext().log("profile update failed",e); Auth.flash(session,"danger","회원정보 수정 중 오류가 발생했습니다."); }
        res.sendRedirect(ctx+"/member.jsp");
    }
    private static String trim(String s){return s==null?"":s.trim();}
}
