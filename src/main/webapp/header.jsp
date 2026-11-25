<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*,com.market.bookitem.Book,com.market.cart.*,com.market.util.Html" %>
<%-- 모든 페이지가 공통으로 쓰는 머리말. 각 페이지는 include 전에 String pageTitle 을 정의해야 합니다. --%>
<%
  String ctx = request.getContextPath();
  String userId = (String) session.getAttribute("userId");
  String uri = request.getRequestURI();
  boolean isAdminUser = "관리자".equals(session.getAttribute("role"));
  int cartCount = 0;
  @SuppressWarnings("unchecked") List<CartItem> navCart = (List<CartItem>) session.getAttribute("cart");
  if (navCart != null) for (CartItem ci : navCart) cartCount += ci.getQuantity();
  String[][] menu = {{"/index.jsp","홈"},{"/bookList.do","도서"},{"/kakaoSearch.jsp","도서 검색"},{"/coupon.jsp","쿠폰"},{"/orderList.jsp","주문내역"}};
  boolean onSearchPage = uri.endsWith("kakaoSearch.jsp");
%><!DOCTYPE html>
<html lang="ko"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title><%=Html.esc(pageTitle)%> | BS's Library</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="<%=ctx%>/css/style.css"></head><body>
<header class="site-header">
  <div class="topbar d-none d-md-block"><div class="container d-flex justify-content-between align-items-center">
    <span><% if (userId != null) { %><b><%=Html.esc(session.getAttribute("memberName"))%></b>님, 환영합니다<% } else { %>회원가입 후 쿠폰함에서 혜택을 확인하세요<% } %></span>
    <span>
    <% if (userId != null) { %>
      <a href="<%=ctx%>/member.jsp">회원정보</a><% if (isAdminUser) { %><a href="<%=ctx%>/admin.jsp">관리자</a><% } %><a href="<%=ctx%>/logout.do">로그아웃</a>
    <% } else { %>
      <a href="<%=ctx%>/login.jsp">로그인</a><a href="<%=ctx%>/join.jsp">회원가입</a>
    <% } %>
    </span>
  </div></div>
  <div class="mainbar"><div class="container">
    <a class="brand" href="<%=ctx%>/index.jsp"><span class="brand-mark" aria-hidden="true"></span>BS's Library</a>
    <% if (!onSearchPage) { %>
    <form class="search d-none d-md-flex" action="<%=ctx%>/kakaoSearch.jsp" method="get" role="search">
      <label class="sr-only" for="hq">도서 검색</label>
      <input id="hq" name="q" type="search" placeholder="책 제목, 저자, 출판사를 검색하세요" autocomplete="off">
      <button type="submit" aria-label="검색"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5"/><path d="M15.5 15.5L21 21"/></svg></button>
    </form>
    <% } %>
    <div class="main-actions">
      <a class="cart-link<%= uri.endsWith("cart.jsp") ? " active" : "" %>" href="<%=ctx%>/cart.jsp" aria-label="장바구니">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 8h14l-1.2 11.2a1 1 0 0 1-1 .8H7.2a1 1 0 0 1-1-.8L5 8z"/><path d="M9 8V6.5a3 3 0 0 1 6 0V8"/></svg>
        <span class="d-none d-sm-inline">장바구니</span><% if (cartCount > 0) { %><span class="nav-count"><%=cartCount%></span><% } %>
      </a>
      <button class="menu-toggle d-md-none" type="button" data-toggle="collapse" data-target="#siteMenu" aria-controls="siteMenu" aria-expanded="false" aria-label="메뉴 열기">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg>
      </button>
    </div>
  </div></div>
  <nav class="menubar" aria-label="주 메뉴"><div class="container">
    <div class="collapse d-md-block" id="siteMenu">
      <ul class="menu">
      <% for (String[] m : menu) {
           boolean on = uri.endsWith(m[0]) || (m[0].equals("/bookList.do") && (uri.endsWith("bookDetail.do")));
      %><li><a class="<%= on ? "active" : "" %>"<%= on ? " aria-current=\"page\"" : "" %> href="<%=ctx%><%=m[0]%>"><%=m[1]%></a></li>
      <% } %>
      <% if (userId != null) { %><li><a href="<%=ctx%>/preferences.jsp">독서 취향</a></li><% } %>
      <% if (isAdminUser) { %><li><a class="<%= uri.endsWith("admin.jsp") ? "active" : "" %>" href="<%=ctx%>/admin.jsp">관리자</a></li><% } %>
      </ul>
      <div class="menu-account d-md-none">
      <% if (userId != null) { %>
        <a href="<%=ctx%>/member.jsp"><%=Html.esc(session.getAttribute("memberName"))%>님</a><a href="<%=ctx%>/logout.do">로그아웃</a>
      <% } else { %>
        <a href="<%=ctx%>/login.jsp">로그인</a><a href="<%=ctx%>/join.jsp">회원가입</a>
      <% } %>
      </div>
    </div>
  </div></nav>
</header>
<%
  String dbError = (String) application.getAttribute("dbError");
  String flashMsg = (String) session.getAttribute("flashMsg"), flashType = (String) session.getAttribute("flashType");
  session.removeAttribute("flashMsg"); session.removeAttribute("flashType");
  if (dbError != null) { %>
<div class="container flash"><div class="alert alert-danger"><b>데이터베이스에 연결하지 못했습니다.</b><br>
WEB-INF/app.properties 의 db.url / db.user / db.password 와 WEB-INF/lib 의 JDBC 드라이버를 확인한 뒤 서버를 다시 시작하세요.<br><small class="text-muted"><%=Html.esc(dbError)%></small></div></div>
<% }
  if (flashMsg != null) { %>
<div class="container flash"><div class="alert alert-<%=Html.esc(flashType == null ? "info" : flashType)%>" role="status"><%=Html.esc(flashMsg)%></div></div>
<% } %>
