<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% if (!"관리자".equals(session.getAttribute("role"))) { response.sendRedirect(request.getContextPath() + "/login.jsp?error=admin"); return; } %>
<% String pageTitle = "도서 등록"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>새 도서 등록</h2>
<% if ("1".equals(request.getParameter("error"))) { %><div class="alert alert-danger">도서를 등록하지 못했습니다. 도서 ID가 이미 있거나 가격이 숫자가 아닐 수 있어요.</div><% } %>
<div class="card p-4" style="max-width:720px"><form action="<%=ctx%>/adminBook.do" method="post">
  <input type="hidden" name="action" value="add">
  <div class="form-group"><label for="f1">도서 ID</label><input class="form-control" id="f1" name="id" placeholder="ISBN1237" required><small class="form-text">표지 이미지는 images/도서ID.jpg 로 넣으면 표시됩니다.</small></div>
  <div class="form-group"><label for="f2">도서명</label><input class="form-control" id="f2" name="name" required></div>
  <div class="form-group"><label for="f3">가격</label><input class="form-control" id="f3" type="number" min="0" name="price" required></div>
  <div class="form-group"><label for="f4">저자</label><input class="form-control" id="f4" name="author"></div>
  <div class="form-group"><label for="f5">설명</label><textarea class="form-control" id="f5" name="description" rows="3"></textarea></div>
  <div class="form-row"><div class="form-group col-md-6"><label for="f6">분야</label><input class="form-control" id="f6" name="category"></div>
  <div class="form-group col-md-6"><label for="f7">출판일</label><input class="form-control" id="f7" name="date" placeholder="2026/01/31"></div></div>
  <button class="btn btn-primary">도서 등록</button> <a class="btn btn-secondary" href="<%=ctx%>/admin.jsp">취소</a>
</form></div></div>
<%@ include file="footer.jsp" %>
