<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "장바구니"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>장바구니</h2>
<% @SuppressWarnings("unchecked") List<CartItem> items = (List<CartItem>) session.getAttribute("cart");
   if (items == null) items = new ArrayList<CartItem>(); int total = 0; %>
<div class="table-responsive"><table class="table">
  <thead><tr><th>도서명</th><th class="text-right">가격</th><th class="text-center">수량</th><th class="text-right">소계</th><th></th></tr></thead><tbody>
<% for (int i = 0; i < items.size(); i++) { CartItem ci = items.get(i); total += ci.getTotalPrice(); %>
  <tr><td><%=Html.esc(ci.getItemBook().getName())%></td><td class="text-right"><%=String.format("%,d", ci.getItemBook().getUnitPrice())%>원</td><td class="text-center"><%=ci.getQuantity()%></td><td class="text-right"><%=String.format("%,d", ci.getTotalPrice())%>원</td>
  <td class="text-right"><form action="<%=ctx%>/cart.do" method="post"><input type="hidden" name="action" value="remove"><input type="hidden" name="index" value="<%=i%>"><button class="btn btn-danger btn-sm">삭제</button></form></td></tr>
<% } if (items.isEmpty()) { %><tr><td colspan="5" class="text-center text-muted py-5">장바구니가 비어 있습니다. 마음에 드는 책을 담아 보세요.</td></tr><% } %>
</tbody></table></div>
<div class="d-flex flex-wrap justify-content-between align-items-center">
  <div>
    <a class="btn btn-secondary" href="<%=ctx%>/bookList.do">쇼핑 계속하기</a>
    <% if (!items.isEmpty()) { %><form class="d-inline" action="<%=ctx%>/cart.do" method="post"><input type="hidden" name="action" value="clear"><button class="btn btn-outline-danger">전체 비우기</button></form><% } %>
  </div>
  <div class="text-right"><div class="price" style="font-size:1.5rem">합계 <%=String.format("%,d", total)%>원</div>
    <% if (!items.isEmpty()) { %><small class="text-muted">쿠폰은 주문서에서 적용할 수 있어요.</small><br>
    <a class="btn btn-success mt-2" href="<%=ctx%>/order.jsp">주문하기</a><% } %></div>
</div></div>
<%@ include file="footer.jsp" %>
