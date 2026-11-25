package com.market.page;
import java.io.*; import java.util.*; import jakarta.servlet.*; import jakarta.servlet.annotation.*; import jakarta.servlet.http.*;
import com.market.bookitem.*; import com.market.cart.*; import java.net.URLEncoder; import java.nio.charset.StandardCharsets;
@WebServlet("/cart.do")
public class CartController extends HttpServlet {
 @SuppressWarnings("unchecked") protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
  req.setCharacterEncoding("UTF-8"); Catalog.init(getServletContext()); HttpSession s=req.getSession();
  List<CartItem> cart=(List<CartItem>)s.getAttribute("cart"); if(cart==null){cart=new ArrayList<>();s.setAttribute("cart",cart);}
  String action=req.getParameter("action");
  if("add".equals(action)){ Book b=Catalog.find(req.getParameter("id")); addToCart(cart,b); }
  else if("addExternal".equals(action)) {
   String id=safe(req.getParameter("bookId"),50), title=safe(req.getParameter("title"),255);
   String author=safe(req.getParameter("author"),255), description=safe(req.getParameter("description"),1000);
   String category=safe(req.getParameter("category"),100), release=safe(req.getParameter("releaseDate"),30);
   int price=0; try { price=Integer.parseInt(req.getParameter("price")); } catch(Exception ignored) {}
   if(!id.isBlank() && !title.isBlank() && price>=0 && price<=100000000) {
    Book external=new Book(id,title,price,author,description,category,release);
    Catalog.addIfAbsent(external);
    addToCart(cart,Catalog.find(id));
   }
   String loggedUser=(String)s.getAttribute("userId");
   if(loggedUser!=null) { try { CartDao.save(loggedUser,cart); } catch(Exception e) { log("cart save failed",e); } }
   String returnTo=req.getParameter("returnTo");
   if("bookList".equals(returnTo)) { res.sendRedirect(req.getContextPath()+"/bookList.do?added=1"); return; }
   if("index".equals(returnTo)) { res.sendRedirect(req.getContextPath()+"/index.jsp?added=1"); return; }
   String q=req.getParameter("q");
   if(q!=null && !q.isBlank()) { res.sendRedirect(req.getContextPath()+"/kakaoSearch.jsp?q="+URLEncoder.encode(q,StandardCharsets.UTF_8)+"&added=1"); return; }
  }
  else if("remove".equals(action)){int i=Integer.parseInt(req.getParameter("index"));if(i>=0&&i<cart.size())cart.remove(i);}
  else if("clear".equals(action))cart.clear();
  String uid=(String)s.getAttribute("userId");
  if(uid!=null){ try{ CartDao.save(uid,cart); }catch(Exception e){ log("cart save failed",e); } }
  res.sendRedirect(req.getContextPath()+"/cart.jsp");
 }
 private static void addToCart(List<CartItem> cart,Book b) {
  if(b==null)return;
  for(CartItem ci:cart) if(ci.getBookID().equals(b.getBookId())) { ci.setQuantity(ci.getQuantity()+1); return; }
  cart.add(new CartItem(b));
 }
 private static String safe(String value,int max) {
  if(value==null)return "";
  value=value.trim();
  return value.length()>max?value.substring(0,max):value;
 }
}
