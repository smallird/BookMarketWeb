<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "독서 취향 설정"; %><%@ include file="header.jsp" %>
<%
String saved=(String)session.getAttribute("genrePreferences");
java.util.Set<String> chosen=new java.util.HashSet<String>();
if(saved!=null) for(String g:saved.split(",")) chosen.add(g);
String[] genres={"소설","에세이","자기계발","경제·경영","인문","역사","과학","IT·컴퓨터","만화","여행","외국어","취미·실용"};
String keywords=(String)session.getAttribute("interestKeywords");
%>
<div class="container mt-4"><div class="card p-4" style="max-width:760px;margin:0 auto">
<h2>나의 독서 취향</h2><p class="text-muted">관심 있는 장르를 선택하면 홈 화면에서 취향에 맞는 책을 먼저 보여드려요.</p>
<% if("db".equals(request.getParameter("error"))) { %><div class="alert alert-danger">저장하지 못했습니다. 다시 시도해 주세요.</div><% } %>
<form action="<%=ctx%>/preferences.do" method="post">
<div class="form-group"><label>선호 장르</label><div class="genre-options">
<% for(String g:genres){ %><label class="genre-option"><input type="checkbox" name="genres" value="<%=Html.esc(g)%>" <%=chosen.contains(g)?"checked":""%>> <%=Html.esc(g)%></label><% } %>
</div></div>
<div class="form-group"><label for="keywords">관심 키워드</label><input class="form-control" id="keywords" name="keywords" maxlength="500" value="<%=Html.esc(keywords)%>" placeholder="예: 프로그래밍, 추리, 일본 여행"></div>
<button class="btn btn-primary">취향 저장하기</button> <a class="btn btn-secondary" href="<%=ctx%>/index.jsp">나중에 하기</a>
</form></div></div>
<%@ include file="footer.jsp" %>
