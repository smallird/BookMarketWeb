package com.market.page;
import java.io.*; import jakarta.servlet.*; import jakarta.servlet.annotation.*; import jakarta.servlet.http.*; import com.market.bookitem.Catalog;
@WebServlet("/bookList.do")
public class BookController extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
  Catalog.init(getServletContext()); req.setAttribute("books",Catalog.all()); req.getRequestDispatcher("/bookList.jsp").forward(req,res);
 }
}
