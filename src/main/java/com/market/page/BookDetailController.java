package com.market.page;
import java.io.*; import jakarta.servlet.*; import jakarta.servlet.annotation.*; import jakarta.servlet.http.*; import com.market.bookitem.*;
@WebServlet("/bookDetail.do")
public class BookDetailController extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
  Catalog.init(getServletContext()); req.setAttribute("book",Catalog.find(req.getParameter("id"))); req.getRequestDispatcher("/bookDetail.jsp").forward(req,res);
 }
}
