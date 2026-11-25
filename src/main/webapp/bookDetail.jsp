<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "도서 상세정보"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><% Book b = (Book) request.getAttribute("book"); if (b != null) { %>
  <div class="card"><div class="row no-gutters">
    <div class="col-md-5"><img class="img-fluid w-100" style="background:var(--panel);padding:32px" alt="" src="<%=ctx%>/images/<%=Html.esc(b.getBookId())%>.jpg" onerror="this.onerror=null;this.src='<%=ctx%>/images/ISBN1234.jpg'"></div>
    <div class="col-md-7 p-4">
      <span class="chip"><%=Html.esc(b.getCategory())%></span>
      <h2 class="mt-2"><%=Html.esc(b.getName())%></h2>
      <p class="text-muted mb-1">저자 <%=Html.esc(b.getAuthor())%> · 출판일 <%=Html.esc(b.getReleaseDate())%> · 도서 ID <%=Html.esc(b.getBookId())%></p>
      <p class="my-3"><%=Html.esc(b.getDescription())%></p>
      <div class="price mb-3" style="font-size:1.7rem"><%=String.format("%,d", b.getUnitPrice())%><small>원</small></div>
      <form class="d-inline" action="<%=ctx%>/cart.do" method="post"><input type="hidden" name="action" value="add"><input type="hidden" name="id" value="<%=Html.esc(b.getBookId())%>"><button class="btn btn-success">장바구니 담기</button></form>
      <a class="btn btn-secondary" href="<%=ctx%>/bookList.do">목록으로</a>
    </div></div></div>
<% } else { %><div class="alert alert-warning">도서를 찾을 수 없습니다.</div><a class="btn btn-secondary" href="<%=ctx%>/bookList.do">목록으로</a><% } %></div>
<%@ include file="footer.jsp" %>
