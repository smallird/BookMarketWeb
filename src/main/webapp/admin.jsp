<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% if (!"관리자".equals(session.getAttribute("role"))) { response.sendRedirect(request.getContextPath() + "/login.jsp?error=admin"); return; } %>
<% String pageTitle = "관리자 메뉴"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>관리자 제어판</h2>
  <div class="row">
    <div class="col-md-4 mb-3"><a class="entry" href="<%=ctx%>/bookForm.jsp"><h4>도서 등록</h4><p>새로운 도서를 추가합니다.</p></a></div>
    <div class="col-md-4 mb-3"><a class="entry" href="<%=ctx%>/manageBooks.do"><h4>도서 관리</h4><p>등록된 도서를 확인하고 삭제합니다.</p></a></div>
    <div class="col-md-4 mb-3"><a class="entry" href="<%=ctx%>/couponAdmin.jsp"><h4>쿠폰 관리</h4><p>쿠폰을 만들고 중지·삭제합니다.</p></a></div>
  </div>
  <a class="btn btn-secondary" href="<%=ctx%>/orderList.jsp">전체 주문내역</a>
  <a class="btn btn-secondary" href="<%=ctx%>/index.jsp">메인으로</a>
</div>
<%@ include file="footer.jsp" %>
