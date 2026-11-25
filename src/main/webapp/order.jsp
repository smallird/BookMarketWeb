<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.market.coupon.*" %>
<% String pageTitle = "주문하기"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>주문하기</h2>
<% if (userId == null) { %>
  <div class="alert alert-warning">주문하려면 로그인해야 합니다.</div><a class="btn btn-primary" href="<%=ctx%>/login.jsp">로그인</a>
<% } else {
   @SuppressWarnings("unchecked") List<CartItem> items = (List<CartItem>) session.getAttribute("cart");
   if (items == null || items.isEmpty()) { %>
  <div class="alert alert-warning">장바구니가 비어 있습니다.</div><a class="btn btn-primary" href="<%=ctx%>/bookList.do">도서 보러 가기</a>
<% } else {
   int subtotal = 0; for (CartItem ci : items) subtotal += ci.getTotalPrice();
   List<UserCoupon> mine = new ArrayList<UserCoupon>();
   try { mine = CouponDao.listForUser(userId); } catch (Exception e) { /* 쿠폰 없이 주문은 가능 */ }
%>
<form action="<%=ctx%>/order.do" method="post"><div class="row">
  <div class="col-lg-7 mb-4">
    <div class="card p-4 mb-4"><h5 class="mb-3">배송 정보</h5>
      <div class="form-group"><label for="n">받는 분</label><input class="form-control" id="n" name="name" value="<%=Html.esc(session.getAttribute("memberName"))%>" required></div>
      <div class="form-group"><label for="p">연락처</label><input class="form-control" id="p" name="phone" value="<%=Html.esc(session.getAttribute("memberPhone"))%>" required></div>
      <div class="form-group mb-0"><label for="a">배송 주소</label><input class="form-control" id="a" name="address" value="<%=Html.esc(session.getAttribute("memberAddress"))%>" required></div>
    </div>
    <h5 class="mb-3">쿠폰</h5>
    <label class="coupon-pick"><input type="radio" name="couponCode" value="" data-discount="0" checked>
      <div class="ticket plain" style="min-height:64px"><div class="ticket-main" style="border:0;display:flex;align-items:center"><h5 class="m-0">쿠폰 사용 안 함</h5></div></div></label>
    <% for (UserCoupon uc : mine) { Coupon k = uc.getCoupon();
         boolean okNow = uc.isAvailable() && subtotal >= k.getMinOrder();
         String why = uc.isAvailable() ? (okNow ? "" : String.format("%,d", k.getMinOrder()) + "원 이상 주문 시 사용 가능") : uc.getStatus(); %>
    <label class="coupon-pick"><input type="radio" name="couponCode" value="<%=Html.esc(k.getCode())%>" data-discount="<%= okNow ? k.discountFor(subtotal) : 0 %>" <%= okNow ? "" : "disabled" %>>
      <div class="ticket<%= Coupon.AMOUNT.equals(k.getType()) ? " is-amount" : "" %><%= okNow ? "" : " is-off" %>">
        <div class="ticket-stub"><b><%= Coupon.PERCENT.equals(k.getType()) ? k.getValue() + "%" : String.format("%,d", k.getValue()) %></b><span><%= Coupon.PERCENT.equals(k.getType()) ? "할인" : "원 할인" %></span></div>
        <div class="ticket-main"><h5><%=Html.esc(k.getName())%></h5><p><%=Html.esc(k.getBenefit())%> · <%=Html.esc(k.getCondition())%></p>
          <p><%= okNow ? "이 주문에서 <b>" + String.format("%,d", k.discountFor(subtotal)) + "원</b> 할인" : Html.esc(why) %></p></div></div></label>
    <% } %>
    <% if (mine.isEmpty()) { %><p class="text-muted">보유한 쿠폰이 없어요. <a href="<%=ctx%>/coupon.jsp">쿠폰 코드 등록하기</a></p><% } %>
  </div>
  <div class="col-lg-5"><div class="card p-4 summary"><h5 class="mb-3">주문 요약</h5>
    <% for (CartItem ci : items) { %><div class="sum-row"><span><%=Html.esc(ci.getItemBook().getName())%> × <%=ci.getQuantity()%></span><span><%=String.format("%,d", ci.getTotalPrice())%>원</span></div><% } %>
    <div class="sum-row" style="border-top:1px solid var(--line);margin-top:.5rem;padding-top:.7rem"><span>상품 금액</span><span><%=String.format("%,d", subtotal)%>원</span></div>
    <div class="sum-row discount"><span>쿠폰 할인</span><span id="disc">0원</span></div>
    <div class="sum-row total"><span>결제 금액</span><span id="tot"><%=String.format("%,d", subtotal)%>원</span></div>
    <button class="btn btn-success btn-block mt-3">주문 완료</button>
  </div></div>
</div></form>
<script>
(function(){
  var sub=<%=subtotal%>, f=function(n){return n.toLocaleString('ko-KR')+'원';};
  document.querySelectorAll('input[name=couponCode]').forEach(function(r){
    r.addEventListener('change',function(){
      var d=Number(r.dataset.discount||0);
      document.getElementById('disc').textContent=d>0?'-'+f(d):'0원';
      document.getElementById('tot').textContent=f(sub-d);
    });
  });
})();
</script>
<% } } %></div>
<%@ include file="footer.jsp" %>
