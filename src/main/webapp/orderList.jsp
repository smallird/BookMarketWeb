<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "주문 내역"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>주문 내역</h2>
<% if (userId == null) { %>
  <div class="alert alert-warning">로그인 후 주문 내역을 확인할 수 있습니다.</div><a class="btn btn-primary" href="<%=ctx%>/login.jsp">로그인</a>
<% } else {
   List<Order> list = new ArrayList<Order>();
   try { list = OrderDao.list(isAdminUser ? null : userId); }
   catch (Exception e) { %><div class="alert alert-danger">주문 내역을 불러오지 못했습니다.</div><% }
   if (list.isEmpty()) { %><div class="alert alert-warning">주문 내역이 없습니다.</div><a class="btn btn-primary" href="<%=ctx%>/bookList.do">도서 둘러보기</a><% }
   for (Order o : list) { %>
  <div class="card mb-4"><div class="card-body">
    <div class="d-flex justify-content-between align-items-baseline flex-wrap"><h5 class="mb-0"><%=Html.esc(o.getOrderNo())%></h5><span class="text-muted"><%=Html.esc(o.getDate())%></span></div>
    <p class="text-muted small my-2">받는 분 <%=Html.esc(o.getName())%> · <%=Html.esc(o.getPhone())%> · <%=Html.esc(o.getAddress())%><% if (isAdminUser) { %> · 주문자 ID <%=Html.esc(o.getUserId())%><% } %></p>
    <div class="table-responsive"><table class="table table-sm mb-2"><thead><tr><th>도서명</th><th class="text-right">수량</th><th class="text-right">금액</th></tr></thead><tbody>
      <% for (CartItem c : o.getItems()) { %><tr><td><%=Html.esc(c.getItemBook().getName())%></td><td class="text-right"><%=c.getQuantity()%>권</td><td class="text-right"><%=String.format("%,d", c.getTotalPrice())%>원</td></tr><% } %>
    </tbody></table></div>
    <div class="text-right">
      <% if (o.getDiscount() > 0) { %><div class="text-muted">상품 금액 <%=String.format("%,d", o.getSubtotal())%>원</div><div style="color:var(--bronze-ink);font-weight:600"><%=Html.esc(o.getCouponName())%> -<%=String.format("%,d", o.getDiscount())%>원</div><% } %>
      <div class="price" style="font-size:1.25rem">결제 금액 <%=String.format("%,d", o.getTotal())%>원</div>
    </div>
  </div></div>
<% } } %></div>
<%@ include file="footer.jsp" %>
