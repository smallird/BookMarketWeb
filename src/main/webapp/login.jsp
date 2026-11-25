<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "로그인"; %><%@ include file="header.jsp" %>
<%
  String err = request.getParameter("error"), errMsg = null;
  if ("fail".equals(err)) errMsg = "아이디 또는 비밀번호가 맞지 않습니다. 다시 확인해 주세요.";
  else if ("db".equals(err)) errMsg = "서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.";
  else if ("kakao".equals(err)) errMsg = "카카오 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.";
  else if ("kakaoCancel".equals(err)) errMsg = "카카오 로그인을 취소했습니다.";
  else if ("admin".equals(err)) errMsg = "관리자 계정으로 로그인해야 열 수 있는 화면입니다.";
%>
<div class="container"><div class="auth-wrap"><div class="card">
  <h2 class="text-center" style="margin-bottom:1.4rem">로그인</h2>
  <% if (errMsg != null) { %><div class="alert alert-danger" role="alert"><%=errMsg%></div><% } %>

  <a class="btn btn-kakao" href="<%=ctx%>/kakaoLogin.do">
    <svg width="20" height="19" viewBox="0 0 20 19" aria-hidden="true"><path fill="#191600" d="M10 0C4.48 0 0 3.56 0 7.95c0 2.84 1.87 5.33 4.69 6.74l-.96 3.5c-.08.3.26.54.52.37l4.17-2.77c.19.02.38.03.58.03 5.52 0 10-3.56 10-7.87S15.52 0 10 0z"/></svg>
    카카오로 로그인
  </a>

  <div class="divider">또는 아이디로 로그인</div>

  <form action="<%=ctx%>/login.do" method="post">
    <div class="form-group"><label for="id">아이디</label><input class="form-control" id="id" name="id" autocomplete="username" required></div>
    <div class="form-group"><label for="pw">비밀번호</label><input class="form-control" id="pw" type="password" name="password" autocomplete="current-password" required></div>
    <button class="btn btn-primary btn-block">로그인</button>
  </form>
  <p class="text-center mt-3 mb-0">아직 회원이 아니신가요? <a href="<%=ctx%>/join.jsp">회원가입</a></p>
  <p class="text-muted small text-center mt-3 mb-0">테스트 계정: admin / 1234 · user / 1234</p>
</div></div></div>
<%@ include file="footer.jsp" %>
