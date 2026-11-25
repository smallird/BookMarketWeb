<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.market.util.Html" %>
<% String pageTitle = "회원정보"; %><%@ include file="header.jsp" %>
<div class="container mt-4 mb-5"><h2>회원정보 수정</h2>
<% if (userId == null) { %>
  <div class="alert alert-warning">회원정보를 수정하려면 먼저 로그인해 주세요.</div><a class="btn btn-primary" href="<%=ctx%>/login.jsp">로그인하기</a>
<% } else { %>
  <div class="alert alert-warning" role="alert">
    <strong>회원정보 변경 횟수 안내</strong><br>
    전화번호·이름·주소 등 기본 회원정보는 한 달에 최대 <strong>3회</strong> 변경할 수 있습니다.<br>
    아이디는 변경한 날부터 <strong>30일이 지나야</strong> 다시 변경할 수 있습니다. 변경 전에 입력 내용을 꼭 확인해 주세요.
  </div>
  <div class="card p-4" style="max-width:680px">
    <form action="<%=ctx%>/profile.do" method="post">
      <div class="form-group"><label for="profileId">아이디</label><input class="form-control" id="profileId" name="id" value="<%=Html.esc(userId)%>" required minlength="4" maxlength="50" pattern="[A-Za-z0-9_]+"><small class="form-text text-muted">영문·숫자·밑줄을 사용해 4~50자로 입력하세요. 변경 후 30일이 지나야 다시 변경할 수 있습니다.</small></div>
      <div class="form-group"><label for="profileName">이름 / 닉네임</label><input class="form-control" id="profileName" name="name" value="<%=Html.esc(session.getAttribute("memberName"))%>" required maxlength="100"></div>
      <div class="form-group"><label for="profilePhone">전화번호</label><input class="form-control" id="profilePhone" name="phone" type="tel" value="<%=Html.esc(session.getAttribute("memberPhone"))%>" maxlength="30" placeholder="010-0000-0000"></div>
      <div class="form-group"><label for="profileAddress">주소</label><input class="form-control" id="profileAddress" name="address" value="<%=Html.esc(session.getAttribute("memberAddress"))%>" maxlength="255"></div>
      <p class="text-muted small">로그인 방식: <strong><%= "kakao".equals(session.getAttribute("loginType")) ? "카카오 로그인" : "아이디 로그인" %></strong></p>
      <button class="btn btn-primary" type="submit">변경사항 저장</button>
      <a class="btn btn-outline-secondary" href="<%=ctx%>/preferences.jsp">독서 취향 수정</a>
    </form>
    <hr>
    <div class="d-flex flex-wrap" style="gap:.5rem"><a class="btn btn-outline-primary" href="<%=ctx%>/coupon.jsp">내 쿠폰</a><a class="btn btn-outline-secondary" href="<%=ctx%>/orderList.jsp">주문내역</a></div>
  </div>
<% } %></div>
<%@ include file="footer.jsp" %>
