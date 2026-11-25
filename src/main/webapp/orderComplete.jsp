<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.market.cart.Order" %>
<% String pageTitle = "주문 완료"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><% Order lo = (Order) session.getAttribute("lastOrder"); if (lo != null) { %>
  <div class="alert alert-success"><h4 class="mb-1">주문이 완료되었습니다.</h4>주문해 주셔서 감사합니다.</div>
  <div class="card p-4 mb-4" style="max-width:520px">
    <div class="sum-row"><span>주문번호</span><b><%=Html.esc(lo.getOrderNo())%></b></div>
    <div class="sum-row"><span>상품 금액</span><span><%=String.format("%,d", lo.getSubtotal())%>원</span></div>
    <% if (lo.getDiscount() > 0) { %><div class="sum-row discount"><span><%=Html.esc(lo.getCouponName())%></span><span>-<%=String.format("%,d", lo.getDiscount())%>원</span></div><% } %>
    <div class="sum-row total"><span>결제 금액</span><span><%=String.format("%,d", lo.getTotal())%>원</span></div>
    <p class="text-muted small mt-3 mb-0">받는 분 <%=Html.esc(lo.getName())%> · <%=Html.esc(lo.getAddress())%></p>
  </div>
<% } else { %><div class="alert alert-warning">최근 주문 정보가 없습니다.</div><% } %>
  <a class="btn btn-primary" href="<%=ctx%>/orderList.jsp">주문 내역 보기</a>
  <a class="btn btn-secondary" href="<%=ctx%>/bookList.do">계속 쇼핑하기</a>
</div>
<%@ include file="footer.jsp" %>
