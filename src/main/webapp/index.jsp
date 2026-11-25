<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*,com.market.bookitem.*,com.market.coupon.*" %>
<% String pageTitle = "홈"; %><%@ include file="header.jsp" %>
<%
  List<Coupon> welcome = new ArrayList<Coupon>();
  List<Book> latest = new ArrayList<Book>();
  List<Book> featured = new ArrayList<Book>();
  List<Book> recommended = new ArrayList<Book>();
  String preferredGenres = (String)session.getAttribute("genrePreferences");
  String preferredKeywords = (String)session.getAttribute("interestKeywords");
  if (application.getAttribute("dbError") == null) {
    try {
      for (Coupon k : CouponDao.listAll()) if (k.isActive() && k.isAutoIssue() && !k.isExpired()) welcome.add(k);
      List<Book> all = Catalog.all();
      featured = all.subList(0, Math.min(3, all.size()));
      latest = all.subList(Math.max(0, all.size() - 4), all.size());
      recommended = Catalog.recommend(preferredGenres, preferredKeywords, 4);
    } catch (Exception e) { /* 화면은 계속 보여줍니다 */ }
  }
%>
<section class="hero"><div class="container hero-grid">
  <div>
    <% if (userId != null) { %><span class="hero-hello"><%=Html.esc(session.getAttribute("memberName"))%>님, 어서 오세요</span><% } %>
    <h1>IT 전문서부터 입문서까지,<br>필요한 책을 한곳에서</h1>
    <p class="lead">관심 있는 책을 발견하고, 나만의 독서 취향에 맞는 도서를 만나보세요.</p>
    <a class="btn btn-success" href="<%=ctx%>/bookList.do">도서 둘러보기</a>
    <% if (userId == null) { %><a class="btn btn-secondary" href="<%=ctx%>/join.jsp">가입하고 쿠폰 받기</a>
    <% } else { %><a class="btn btn-secondary" href="<%=ctx%>/preferences.jsp">독서 취향 설정</a><% } %>
  </div>
  <% if (!featured.isEmpty()) { %>
  <div class="hero-covers">
    <% for (Book b : featured) { %><a class="hc" href="<%=ctx%>/bookDetail.do?id=<%=Html.esc(b.getBookId())%>"><img alt="<%=Html.esc(b.getName())%>" src="<%=ctx%>/images/<%=Html.esc(b.getBookId())%>.jpg" onerror="this.onerror=null;this.src='<%=ctx%>/images/ISBN1234.jpg'"></a><% } %>
  </div>
  <% } %>
</div></section>

<div class="container">
<%-- 맞춤 도서는 카카오 책 검색 결과를 취향에 맞춰 표시합니다. --%>
<div class="section">
  <div class="section-title">
    <div>
      <h2><% if (userId != null && ((preferredGenres != null && !preferredGenres.isBlank()) || (preferredKeywords != null && !preferredKeywords.isBlank()))) { %><%=Html.esc(session.getAttribute("memberName"))%>님을 위한 맞춤 도서<% } else if (userId != null) { %>나를 위한 맞춤 도서<% } else { %>추천 도서<% } %></h2>
      <p class="text-muted small mb-0"><% if (userId != null && ((preferredGenres != null && !preferredGenres.isBlank()) || (preferredKeywords != null && !preferredKeywords.isBlank()))) { %>저장한 장르와 관심 키워드를 카카오 책 검색에 반영했어요.<% } else if (userId != null) { %>독서 취향을 설정하면 선택한 장르와 키워드에 맞춰 추천해 드려요.<% } else { %>카카오 책 검색으로 인기 주제의 도서를 소개합니다. 로그인 후 취향을 설정하면 맞춤 추천을 받을 수 있어요.<% } %></p>
    </div>
    <% if (userId != null) { %><a href="<%=ctx%>/preferences.jsp">취향 수정</a><% } else { %><a href="<%=ctx%>/login.jsp">로그인</a><% } %>
  </div>
  <div id="home-kakao-recommendations" class="row" data-genres="<%=Html.esc(preferredGenres)%>" data-keywords="<%=Html.esc(preferredKeywords)%>" data-context="<%=ctx%>">
    <div class="col-12"><p id="home-kakao-message" class="text-muted">취향에 맞는 도서를 불러오는 중입니다...</p></div>
  </div>
</div>
<script>
(function(){
  var root=document.getElementById('home-kakao-recommendations'); if(!root) return;
  var ctx=root.getAttribute('data-context');
  var genres=(root.getAttribute('data-genres')||'').split(',').map(function(v){return v.trim();}).filter(Boolean);
  var keywords=(root.getAttribute('data-keywords')||'').split(/[,，]/).map(function(v){return v.trim();}).filter(Boolean);
  var genreMap={'소설':'한국 소설','에세이':'에세이','자기계발':'자기계발','경제·경영':'경제 경영','인문':'인문학','역사':'역사','과학':'과학','IT·컴퓨터':'프로그래밍 컴퓨터','만화':'만화','여행':'여행','외국어':'외국어','취미·실용':'취미 실용'};
  var queries=[];
  genres.forEach(function(g){var q=genreMap[g]||g;if(q&&queries.indexOf(q)<0)queries.push(q);});
  keywords.forEach(function(k){if(k&&queries.indexOf(k)<0)queries.push(k);});
  if(!queries.length) queries=['베스트셀러','인기 도서'];
  queries=queries.slice(0,5);
  var msg=document.getElementById('home-kakao-message');
  Promise.all(queries.map(function(q){return fetch(ctx+'/kakaoBook.do?q='+encodeURIComponent(q)+'&page=1').then(function(r){if(!r.ok)throw new Error('Kakao API '+r.status);return r.json();}).then(function(d){return {query:q,documents:d.documents||[]};});}))
  .then(function(groups){
    var seen={},docs=[];
    groups.forEach(function(group){group.documents.forEach(function(d){var key=String(d.isbn||'')+'|'+String(d.title||'');if(!seen[key]){seen[key]=true;docs.push({book:d,matched:group.query});}});});
    root.textContent='';
    docs.slice(0,4).forEach(function(item){
      var d=item.book, col=document.createElement('div');col.className='col-6 col-md-3 mb-4';
      var card=document.createElement('div');card.className='card h-100';
      if(d.thumbnail){var img=document.createElement('img');img.className='card-img-top book-cover';img.alt='';img.src=d.thumbnail;card.appendChild(img);}
      var body=document.createElement('div');body.className='card-body d-flex flex-column';
      var chip=document.createElement('span');chip.className='chip mb-2';chip.textContent=item.matched+' 맞춤';body.appendChild(chip);
      var title=document.createElement('h5');title.textContent=String(d.title||'').replace(/<[^>]*>/g,'');body.appendChild(title);
      var author=document.createElement('p');author.className='text-muted small';author.textContent=(d.authors||[]).join(', ');body.appendChild(author);
      var publisher=document.createElement('p');publisher.className='text-muted small';publisher.textContent=d.publisher||'';body.appendChild(publisher);
      var price=document.createElement('div');price.className='price mb-3';var amount=Number(d.sale_price>=0?d.sale_price:d.price||0);price.textContent=amount>0?amount.toLocaleString()+'원':'가격 정보 없음';body.appendChild(price);
      var actions=document.createElement('div');actions.className='mt-auto d-flex flex-wrap';actions.style.gap='.4rem';
      if(d.url){var link=document.createElement('a');link.className='btn btn-outline-primary btn-sm';link.href=d.url;link.target='_blank';link.rel='noopener';link.textContent='상세 정보';actions.appendChild(link);}
      var form=document.createElement('form');form.action=ctx+'/cart.do';form.method='post';
      var isbn=String(d.isbn||'').split(' ').filter(Boolean);var bookId=isbn.find(function(v){return /^\d{13}$/.test(v);})||isbn.find(function(v){return /^\d{10}$/.test(v);})||('KAKAO-'+Math.abs(String(d.title||'').split('').reduce(function(a,c){return (a*31+c.charCodeAt(0))|0;},7)).toString(36));
      function hidden(n,v){var i=document.createElement('input');i.type='hidden';i.name=n;i.value=String(v==null?'':v);form.appendChild(i);}
      hidden('action','addExternal');hidden('bookId',bookId);hidden('title',String(d.title||'').replace(/<[^>]*>/g,''));hidden('price',amount);hidden('author',(d.authors||[]).join(', '));hidden('description',d.contents||'');hidden('category',item.matched);hidden('releaseDate',String(d.datetime||'').substring(0,10));hidden('returnTo','index');
      var btn=document.createElement('button');btn.type='submit';btn.className='btn btn-success btn-sm';btn.textContent='장바구니 담기';form.appendChild(btn);actions.appendChild(form);body.appendChild(actions);card.appendChild(body);col.appendChild(card);root.appendChild(col);
    });
    if(!docs.length){root.textContent='취향에 맞는 도서를 찾지 못했습니다. 취향 설정에서 다른 장르나 키워드를 추가해 보세요.';}
  }).catch(function(){if(msg)msg.textContent='맞춤 도서를 불러오지 못했습니다. app.properties의 카카오 REST API 키와 서버 연결을 확인해 주세요.';});
})();
</script>

<% if (!latest.isEmpty()) { %>
  <div class="section"><div class="section-title"><h2>최근 들어온 책</h2><a href="<%=ctx%>/bookList.do">전체 보기</a></div><div class="row">
  <% for (Book b : latest) { %>
    <div class="col-6 col-md-3 mb-4"><a class="card h-100 text-decoration-none" href="<%=ctx%>/bookDetail.do?id=<%=Html.esc(b.getBookId())%>" style="color:inherit">
      <img class="card-img-top book-cover" alt="" src="<%=ctx%>/images/<%=Html.esc(b.getBookId())%>.jpg" onerror="this.onerror=null;this.src='<%=ctx%>/images/ISBN1234.jpg'">
      <div class="card-body"><h5><%=Html.esc(b.getName())%></h5><p class="text-muted small mb-2"><%=Html.esc(b.getAuthor())%></p><div class="price"><%=String.format("%,d", b.getUnitPrice())%><small>원</small></div></div></a></div>
  <% } %></div></div>
<% } %>

<% if (!welcome.isEmpty()) { %>
  <div class="section"><div class="section-title"><h2>가입하면 바로 받는 쿠폰</h2><a href="<%=ctx%>/join.jsp">회원가입</a></div><div class="row">
  <% for (Coupon k : welcome) { %>
    <div class="col-lg-6 mb-3"><div class="ticket<%= Coupon.AMOUNT.equals(k.getType()) ? " is-amount" : "" %>">
      <div class="ticket-stub"><b><%= Coupon.PERCENT.equals(k.getType()) ? k.getValue() + "%" : String.format("%,d", k.getValue()) %></b><span><%= Coupon.PERCENT.equals(k.getType()) ? "할인" : "원 할인" %></span></div>
      <div class="ticket-main"><h5><%=Html.esc(k.getName())%></h5><p><%=Html.esc(k.getBenefit())%> · <%=Html.esc(k.getCondition())%></p><p><%=Html.esc(k.getExpiryText())%></p></div>
    </div></div>
  <% } %></div></div>
<% } %>

  <div class="section"><div class="section-title"><h2>이용 안내</h2></div><div class="row">
    <div class="col-md-4 mb-3"><a class="entry" href="<%=ctx%>/bookList.do"><h4>도서 목록</h4><p>등록된 도서와 상세 정보를 확인합니다.</p></a></div>
    <div class="col-md-4 mb-3"><a class="entry" href="<%=ctx%>/coupon.jsp"><h4>쿠폰함</h4><p>쿠폰 코드를 등록하고 보유 쿠폰을 확인합니다.</p></a></div>
    <div class="col-md-4 mb-3"><a class="entry" href="<%=ctx%>/kakaoSearch.jsp"><h4>도서 검색</h4><p>카카오 책 검색으로 더 많은 책을 찾아봅니다.</p></a></div>
  </div></div>
</div>
<%@ include file="footer.jsp" %>
