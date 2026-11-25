<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "도서"; %><%@ include file="header.jsp" %>
<div class="container mt-4">
<% if ("1".equals(request.getParameter("added"))) { %><div class="alert alert-success">추천 도서를 장바구니에 담았습니다.</div><% } %>
<div class="section">
  <div class="section-title"><div><h2><% if(userId != null) { %>회원님 취향에 맞는 추천 도서<% } else { %>카카오 추천 도서<% } %></h2><p class="text-muted small mb-0"><% if(userId != null) { %>선택한 장르와 관심 키워드를 바탕으로 카카오 책 검색에서 찾아왔어요.<% } else { %>인기 주제의 도서를 카카오 책 검색에서 찾아봤어요. 로그인하면 취향에 맞춰 추천해 드립니다.<% } %></p></div><a href="<%=ctx%>/kakaoSearch.jsp">도서 직접 검색</a></div>
  <div id="kakao-recommendations" class="row" data-genres="<%=Html.esc(session.getAttribute("genrePreferences"))%>" data-keywords="<%=Html.esc(session.getAttribute("interestKeywords"))%>" data-context="<%=ctx%>">
    <div class="col-12"><p id="recommend-message" class="text-muted">카카오 도서 정보를 불러오는 중입니다...</p></div>
  </div>
</div>
<div class="section"><h2>전체 도서 목록</h2><div class="row">
<% @SuppressWarnings("unchecked") List<Book> books = (List<Book>) request.getAttribute("books");
   if (books == null || books.isEmpty()) { %><div class="col-12"><div class="alert alert-info">등록된 도서가 없습니다.</div></div><% }
   else for (Book b : books) { %>
  <div class="col-sm-6 col-lg-4 mb-4"><div class="card h-100">
    <img class="card-img-top book-cover" alt="" src="<%=ctx%>/images/<%=Html.esc(b.getBookId())%>.jpg" onerror="this.onerror=null;this.src='<%=ctx%>/images/ISBN1234.jpg'">
    <div class="card-body d-flex flex-column">
      <div class="mb-2"><span class="chip"><%=Html.esc(b.getCategory())%></span></div>
      <h5><%=Html.esc(b.getName())%></h5>
      <p class="text-muted mb-2">저자 <%=Html.esc(b.getAuthor())%></p>
      <div class="price mb-3"><%=String.format("%,d", b.getUnitPrice())%><small>원</small></div>
      <div class="mt-auto">
        <a class="btn btn-outline-primary btn-sm" href="<%=ctx%>/bookDetail.do?id=<%=Html.esc(b.getBookId())%>">상세 정보</a>
        <form class="d-inline" action="<%=ctx%>/cart.do" method="post"><input type="hidden" name="action" value="add"><input type="hidden" name="id" value="<%=Html.esc(b.getBookId())%>"><button class="btn btn-success btn-sm">장바구니 담기</button></form>
      </div>
    </div></div></div>
<% } %></div></div></div>
<script>
(function(){
  var root=document.getElementById('kakao-recommendations');
  if(!root) return;
  var ctx=root.getAttribute('data-context');
  var genres=(root.getAttribute('data-genres')||'').split(',').map(function(v){return v.trim();}).filter(Boolean);
  var keywords=(root.getAttribute('data-keywords')||'').split(/[,，]/).map(function(v){return v.trim();}).filter(Boolean);
  var queries=[];
  var map={'소설':'한국 소설','에세이':'에세이','자기계발':'자기계발','경제·경영':'경제 경영','인문':'인문학','역사':'역사','과학':'과학','IT·컴퓨터':'프로그래밍 컴퓨터','만화':'만화','여행':'여행','외국어':'외국어','취미·실용':'취미 실용'};
  genres.forEach(function(g){ if(map[g] && queries.indexOf(map[g])<0) queries.push(map[g]); });
  keywords.forEach(function(k){if(k && queries.indexOf(k)<0) queries.push(k);});
  if(!queries.length) queries=['베스트셀러','인기 도서'];
  queries=queries.slice(0,4);
  var msg=document.getElementById('recommend-message'), seen={};
  function esc(s){return String(s==null?'':s).replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];});}
  Promise.all(queries.map(function(q){return fetch(ctx+'/kakaoBook.do?q='+encodeURIComponent(q)+'&page=1').then(function(r){if(!r.ok) throw new Error('API');return r.json();}).then(function(data){return {q:q,docs:data.documents||[]};});}))
  .then(function(groups){
    var docs=[]; groups.forEach(function(g){g.docs.forEach(function(d){var key=(d.isbn||'')+'|'+d.title;if(!seen[key]){seen[key]=true;d._query=g.q;docs.push(d);}});});
    docs=docs.slice(0,8); root.innerHTML='';
    if(!docs.length){root.innerHTML='<div class="col-12"><p class="text-muted">추천 도서를 찾지 못했습니다. 도서 검색을 이용해 주세요.</p></div>';return;}
    docs.forEach(function(d){
      var raw=String(d.isbn||'').split(' ').filter(Boolean), id=raw.find(function(v){return /^\d{13}$/.test(v);})||raw.find(function(v){return /^\d{10}$/.test(v);});
      if(!id){var h=7;String(d.title||'').split('').forEach(function(c){h=(Math.imul(h,31)+c.charCodeAt(0))|0;});id='KAKAO-'+(h>>>0).toString(36);}
      var title=String(d.title||'').replace(/<[^>]*>/g,''), author=(d.authors||[]).join(', '), category=String(d.category||'추천').split('>').pop().trim();
      var price=Number(d.sale_price||d.price||0), desc=String(d.contents||''), release=String(d.datetime||'').substring(0,10);
      var card='<div class="col-sm-6 col-lg-3 mb-4"><div class="card h-100">'+(d.thumbnail?'<img class="card-img-top book-cover" alt="" src="'+esc(d.thumbnail)+'">':'<div class="card-img-top book-cover"></div>')+
       '<div class="card-body d-flex flex-column"><span class="chip mb-2">'+esc(category||'추천 도서')+'</span><h5>'+esc(title)+'</h5><p class="text-muted small">'+esc(author)+'</p><p class="text-muted small">추천 검색어: '+esc(d._query)+'</p><div class="price mb-3">'+(price>0?price.toLocaleString()+'원':'가격 정보 없음')+'</div>'+
       '<div class="mt-auto d-flex flex-wrap" style="gap:.4rem"><a class="btn btn-outline-primary btn-sm" target="_blank" rel="noopener" href="'+esc(d.url||'#')+'">상세 정보</a><form action="'+ctx+'/cart.do" method="post"><input type="hidden" name="action" value="addExternal"><input type="hidden" name="bookId" value="'+esc(id)+'"><input type="hidden" name="title" value="'+esc(title)+'"><input type="hidden" name="price" value="'+price+'"><input type="hidden" name="author" value="'+esc(author)+'"><input type="hidden" name="description" value="'+esc(desc)+'"><input type="hidden" name="category" value="'+esc(category)+'"><input type="hidden" name="releaseDate" value="'+esc(release)+'"><input type="hidden" name="returnTo" value="bookList"><button class="btn btn-success btn-sm" type="submit">장바구니 담기</button></form></div></div></div></div>';
      root.insertAdjacentHTML('beforeend',card);
    });
  }).catch(function(){msg.textContent='카카오 추천 도서를 불러오지 못했습니다. 카카오 REST API 키와 서버 연결을 확인해 주세요.';});
})();
</script>
<%@ include file="footer.jsp" %>
