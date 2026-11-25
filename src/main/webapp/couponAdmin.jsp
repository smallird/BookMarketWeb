<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% if (!"관리자".equals(session.getAttribute("role"))) { response.sendRedirect(request.getContextPath() + "/login.jsp?error=admin"); return; } %>
<%@ page import="com.market.coupon.*" %>
<% String pageTitle = "쿠폰 관리"; %><%@ include file="header.jsp" %>
<%
  List<Coupon> coupons = new ArrayList<Coupon>();
  try { coupons = CouponDao.listAll(); } catch (Exception e) { %><div class="container mt-3"><div class="alert alert-danger">쿠폰을 불러오지 못했습니다.</div></div><% }
%>
<div class="container mt-4"><h2>쿠폰 관리</h2>
  <div class="card p-4 mb-4"><h5 class="mb-3">새 쿠폰 만들기</h5>
  <form action="<%=ctx%>/adminCoupon.do" method="post"><input type="hidden" name="action" value="create">
    <div class="form-row">
      <div class="form-group col-md-3"><label for="c1">쿠폰 코드</label><input class="form-control text-uppercase" id="c1" name="code" maxlength="30" placeholder="SPRING5000" required></div>
      <div class="form-group col-md-5"><label for="c2">쿠폰 이름</label><input class="form-control" id="c2" name="name" maxlength="100" placeholder="봄맞이 5,000원 쿠폰" required></div>
      <div class="form-group col-md-2"><label for="c3">할인 방식</label><select class="custom-select" id="c3" name="type"><option value="AMOUNT">정액 (원)</option><option value="PERCENT">정률 (%)</option></select></div>
      <div class="form-group col-md-2"><label for="c4">할인 값</label><input class="form-control" id="c4" name="value" type="number" min="1" required></div>
    </div>
    <div class="form-row">
      <div class="form-group col-md-3"><label for="c5">최소 주문금액(원)</label><input class="form-control" id="c5" name="minOrder" type="number" min="0" value="0"></div>
      <div class="form-group col-md-3"><label for="c6">최대 할인액(원)</label><input class="form-control" id="c6" name="maxDiscount" type="number" min="0" value="0"><small class="form-text">정률 쿠폰에만 적용 · 0은 제한 없음</small></div>
      <div class="form-group col-md-3"><label for="c7">사용 기한</label><input class="form-control" id="c7" name="expiresAt" type="date"><small class="form-text">비우면 기한 없음</small></div>
      <div class="form-group col-md-3 d-flex align-items-center"><div class="custom-control custom-checkbox mt-2"><input type="checkbox" class="custom-control-input" id="c8" name="autoIssue"><label class="custom-control-label" for="c8">가입 시 자동 지급</label></div></div>
    </div>
    <button class="btn btn-primary">쿠폰 만들기</button>
  </form></div>

  <div class="table-responsive"><table class="table"><thead><tr><th>코드</th><th>이름</th><th>혜택</th><th>조건</th><th>기한</th><th>상태</th><th></th></tr></thead><tbody>
  <% for (Coupon k : coupons) { boolean live = k.isActive() && !k.isExpired(); %>
    <tr><td><b><%=Html.esc(k.getCode())%></b><% if (k.isAutoIssue()) { %><br><span class="chip">가입 시 지급</span><% } %></td>
      <td><%=Html.esc(k.getName())%></td><td><%=Html.esc(k.getBenefit())%></td><td><%=Html.esc(k.getCondition())%></td><td><%=Html.esc(k.getExpiryText())%></td>
      <td><%= k.isExpired() ? "기한 만료" : (k.isActive() ? "사용 중" : "중지") %></td>
      <td class="text-right text-nowrap">
        <form class="d-inline" action="<%=ctx%>/adminCoupon.do" method="post"><input type="hidden" name="action" value="toggle"><input type="hidden" name="code" value="<%=Html.esc(k.getCode())%>"><input type="hidden" name="active" value="<%= k.isActive() ? "0" : "1" %>"><button class="btn btn-secondary btn-sm"><%= k.isActive() ? "중지" : "다시 사용" %></button></form>
        <form class="d-inline" action="<%=ctx%>/adminCoupon.do" method="post" onsubmit="return confirm('<%=Html.esc(k.getCode())%> 쿠폰을 삭제할까요? 회원이 아직 안 쓴 쿠폰도 함께 사라집니다.')"><input type="hidden" name="action" value="delete"><input type="hidden" name="code" value="<%=Html.esc(k.getCode())%>"><button class="btn btn-danger btn-sm">삭제</button></form>
      </td></tr>
  <% } if (coupons.isEmpty()) { %><tr><td colspan="7" class="text-center text-muted py-4">만든 쿠폰이 없습니다.</td></tr><% } %>
  </tbody></table></div>
  <a class="btn btn-secondary" href="<%=ctx%>/admin.jsp">관리자 메뉴</a>
</div>
<%@ include file="footer.jsp" %>
