<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "회원가입"; %><%@ include file="header.jsp" %>
<%
  String err = request.getParameter("error"), errMsg = null;
  if ("id".equals(err)) errMsg = "아이디는 영문, 숫자, 밑줄(_)으로 4~20자여야 합니다.";
  else if ("pw".equals(err)) errMsg = "비밀번호는 4자 이상으로 입력해 주세요.";
  else if ("pw2".equals(err)) errMsg = "비밀번호 확인이 일치하지 않습니다.";
  else if ("name".equals(err)) errMsg = "이름을 입력해 주세요.";
  else if ("dup".equals(err)) errMsg = "이미 사용 중인 아이디입니다. 다른 아이디를 입력해 주세요.";
  else if ("db".equals(err)) errMsg = "가입을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.";
%>
<div class="container"><div class="auth-wrap"><div class="card">
  <h2 class="text-center" style="margin-bottom:.4rem">회원가입</h2>
  <p class="text-center text-muted">가입하면 신규 가입 쿠폰이 바로 지급됩니다.</p>
  <% if (errMsg != null) { %><div class="alert alert-danger" role="alert"><%=errMsg%></div><% } %>
  <form action="<%=ctx%>/join.do" method="post">
    <div class="form-group"><label for="id">아이디</label><input class="form-control" id="id" name="id" pattern="[A-Za-z0-9_]{4,20}" autocomplete="username" required><small class="form-text">영문, 숫자, 밑줄 4~20자</small></div>
    <div class="form-group"><label for="name">이름</label><input class="form-control" id="name" name="name" autocomplete="name" required></div>
    <div class="form-group"><label for="pw">비밀번호</label><input class="form-control" id="pw" type="password" name="password" minlength="4" autocomplete="new-password" required></div>
    <div class="form-group"><label for="pw2">비밀번호 확인</label><input class="form-control" id="pw2" type="password" name="password2" minlength="4" autocomplete="new-password" required></div>
    <div class="form-group"><label for="phone">연락처</label><input class="form-control" id="phone" name="phone" placeholder="010-0000-0000" autocomplete="tel"></div>
    <div class="form-group"><label>관심 있는 책 장르</label>
      <small class="form-text mb-2">여러 개를 선택할 수 있어요. 선택한 장르에 맞춰 도서를 추천합니다.</small>
      <div class="genre-options">
      <% String[] genres = {"소설","에세이","자기계발","경제·경영","인문","역사","과학","IT·컴퓨터","만화","여행","외국어","취미·실용"}; 
         for(String g:genres) { %><label class="genre-option"><input type="checkbox" name="genres" value="<%=Html.esc(g)%>"> <%=Html.esc(g)%></label><% } %>
      </div>
    </div>
    <div class="form-group"><label for="keywords">관심 키워드 (선택)</label><input class="form-control" id="keywords" name="keywords" placeholder="예: 프로그래밍, 추리, 일본 여행"></div>
    <div class="form-group"><label for="addr">주소</label><input class="form-control" id="addr" name="address" autocomplete="street-address"></div>
    <button class="btn btn-primary btn-block">가입하기</button>
  </form>
  <div class="divider">또는</div>
  <a class="btn btn-kakao" href="<%=ctx%>/kakaoLogin.do">카카오로 3초 만에 시작하기</a>
  <p class="text-center mt-3 mb-0">이미 회원이신가요? <a href="<%=ctx%>/login.jsp">로그인</a></p>
</div></div></div>
<%@ include file="footer.jsp" %>
