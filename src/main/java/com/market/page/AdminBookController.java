package com.market.page;
import java.io.*; import jakarta.servlet.*; import jakarta.servlet.annotation.*; import jakarta.servlet.http.*; import com.market.bookitem.*;
@WebServlet("/adminBook.do")
public class AdminBookController extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
  if(com.market.member.Auth.denyUnlessAdmin(req,res)) return;
  Catalog.init(getServletContext()); req.setAttribute("books",Catalog.all()); req.getRequestDispatcher("/manageBooks.jsp").forward(req,res);
 }
 protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
  if(com.market.member.Auth.denyUnlessAdmin(req,res)) return;
  req.setCharacterEncoding("UTF-8"); Catalog.init(getServletContext()); String action=req.getParameter("action");
  if("delete".equals(action)) Catalog.remove(req.getParameter("id"));
  else if("add".equals(action)){try{Catalog.add(new Book(req.getParameter("id"),req.getParameter("name"),Integer.parseInt(req.getParameter("price")),req.getParameter("author"),req.getParameter("description"),req.getParameter("category"),req.getParameter("date")));}catch(Exception e){res.sendRedirect(req.getContextPath()+"/bookForm.jsp?error=1");return;}}
  res.sendRedirect(req.getContextPath()+"/manageBooks.do");
 }
}
