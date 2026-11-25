<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% if (!"관리자".equals(session.getAttribute("role"))) { response.sendRedirect(request.getContextPath() + "/login.jsp?error=admin"); return; } %>
<% String pageTitle = "도서 관리"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>등록된 도서 관리</h2>
<div class="table-responsive"><table class="table"><thead><tr><th>ID</th><th>도서명</th><th class="text-right">가격</th><th></th></tr></thead><tbody>
<% @SuppressWarnings("unchecked") List<Book> books = (List<Book>) request.getAttribute("books");
   if (books != null) for (Book b : books) { %>
  <tr><td><%=Html.esc(b.getBookId())%></td><td><%=Html.esc(b.getName())%></td><td class="text-right"><%=String.format("%,d", b.getUnitPrice())%>원</td>
  <td class="text-right"><form action="<%=ctx%>/adminBook.do" method="post" onsubmit="return confirm('이 도서를 삭제할까요?')"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="<%=Html.esc(b.getBookId())%>"><button class="btn btn-danger btn-sm">삭제</button></form></td></tr>
<% } %></tbody></table></div>
<a class="btn btn-primary" href="<%=ctx%>/bookForm.jsp">도서 추가</a> <a class="btn btn-secondary" href="<%=ctx%>/admin.jsp">관리자 메뉴</a></div>
<%@ include file="footer.jsp" %>
