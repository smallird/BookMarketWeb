<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.market.coupon.*" %>
<% String pageTitle = "쿠폰함"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>쿠폰함</h2>
<% if (userId == null) { %>
  <div class="alert alert-warning">쿠폰함을 보려면 먼저 로그인해 주세요.</div><a class="btn btn-primary" href="<%=ctx%>/login.jsp">로그인</a>
<% } else {
   List<UserCoupon> mine = new ArrayList<UserCoupon>();
   try { mine = CouponDao.listForUser(userId); } catch (Exception e) { %><div class="alert alert-danger">쿠폰을 불러오지 못했습니다.</div><% } %>
  <div class="card p-3 mb-4"><form class="form-inline" action="<%=ctx%>/coupon.do" method="post">
    <label class="lbl mr-3 mb-0" for="code">쿠폰 코드 등록</label>
    <input class="form-control mr-2 text-uppercase" id="code" name="code" placeholder="예: BOOK3000" maxlength="30" style="min-width:220px" required>
    <button class="btn btn-primary">등록</button>
  </form></div>
  <div class="row">
  <% for (UserCoupon uc : mine) { Coupon k = uc.getCoupon(); %>
    <div class="col-lg-6 mb-3"><div class="ticket<%= Coupon.AMOUNT.equals(k.getType()) ? " is-amount" : "" %><%= uc.isAvailable() ? "" : " is-off" %>">
      <div class="ticket-stub"><b><%= Coupon.PERCENT.equals(k.getType()) ? k.getValue() + "%" : String.format("%,d", k.getValue()) %></b><span><%= Coupon.PERCENT.equals(k.getType()) ? "할인" : "원 할인" %></span></div>
      <div class="ticket-main"><span class="state"><%=uc.getStatus()%></span><h5><%=Html.esc(k.getName())%></h5>
        <p><%=Html.esc(k.getBenefit())%> · <%=Html.esc(k.getCondition())%></p><p><%=Html.esc(k.getExpiryText())%></p><span class="code"><%=Html.esc(k.getCode())%></span></div>
    </div></div>
  <% } %></div>
  <% if (mine.isEmpty()) { %><div class="alert alert-info">아직 보유한 쿠폰이 없어요. 위에서 쿠폰 코드를 등록해 보세요.</div><% } %>
  <a class="btn btn-success" href="<%=ctx%>/bookList.do">쿠폰 쓰러 가기</a>
<% } %></div>
<%@ include file="footer.jsp" %>
