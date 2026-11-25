<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% String pageTitle = "도서 검색"; %><%@ include file="header.jsp" %>
<div class="container mt-4"><h2>도서 검색 <small class="text-muted" style="font-family:var(--sans);font-size:.9rem;font-weight:500">카카오 책 검색</small></h2>
<form class="form-inline mb-3" onsubmit="search(true);return false;">
  <label class="sr-only" for="q">검색어</label>
  <input id="q" class="form-control mr-2 mb-2" style="min-width:300px" placeholder="책 제목, 저자, 출판사를 입력하세요" autofocus>
  <button class="btn btn-primary mb-2">검색</button>
</form>
<p id="msg" class="text-muted"></p>
<% if ("1".equals(request.getParameter("added"))) { %><div class="alert alert-success">검색한 도서를 장바구니에 담았습니다.</div><% } %>
<div id="result" class="row"></div>
<div class="text-center"><button id="more" class="btn btn-secondary" style="display:none" onclick="search(false)">더 보기</button></div>
</div>
<script>
var page=1, query='';
(function(){
  var q=new URLSearchParams(location.search).get('q');
  if(q&&q.trim()){ document.getElementById('q').value=q.trim(); document.addEventListener('DOMContentLoaded',function(){ search(true); }); }
})();
function esc(s){
  return String(s==null?'':s).replace(/[&<>"']/g,function(c){
    return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];
  });
}
function search(reset){
  var msg=document.getElementById('msg'), box=document.getElementById('result'), more=document.getElementById('more');
  if(reset){
    query=document.getElementById('q').value.trim(); page=1; box.innerHTML='';
  }
  if(!query){ msg.textContent='검색어를 입력하세요.'; return; }
  msg.textContent='검색 중입니다...'; more.style.display='none';

  fetch('<%=ctx%>/kakaoBook.do?q='+encodeURIComponent(query)+'&page='+page)
    .then(function(r){ return r.json().then(function(j){ return {ok:r.ok, j:j}; }); })
    .then(function(o){
      if(!o.ok){ msg.textContent='오류: '+(o.j.message||o.j.error||'검색에 실패했습니다.'); return; }
      var docs=o.j.documents||[];
      if(docs.length===0 && page===1){ msg.textContent='검색 결과가 없습니다.'; return; }
      msg.textContent='총 '+Number(o.j.meta.total_count).toLocaleString()+'건';
      docs.forEach(function(d){
        var price = d.price>0 ? Number(d.price).toLocaleString()+'원' : '가격 정보 없음';
        var isbnRaw=String(d.isbn||'').split(' ').filter(Boolean);
        var isbn=isbnRaw.find(function(v){return /^\d{13}$/.test(v);})||isbnRaw.find(function(v){return /^\d{10}$/.test(v);});
        if(!isbn) isbn='KAKAO-'+(function(){var h=7; (String(d.title)+String((d.authors||[])[0]||'')).split('').forEach(function(c){h=(Math.imul(h,31)+c.charCodeAt(0))|0;}); return (h>>>0).toString(36);})();
        var title=String(d.title||'').replace(/<[^>]*>/g,'');
        var author=(d.authors||[]).join(', ');
        var category=String(d.category||'기타').split('>').pop().trim();
        var salePrice=Number(d.sale_price||d.price||0);
        var desc=String(d.contents||'');
        var release=String(d.datetime||'').substring(0,10);
        var form='<form class="search-add-form" action="<%=ctx%>/cart.do" method="post">'
          +'<input type="hidden" name="action" value="addExternal">'
          +'<input type="hidden" name="bookId" value="'+esc(isbn)+'">'
          +'<input type="hidden" name="title" value="'+esc(title)+'">'
          +'<input type="hidden" name="price" value="'+salePrice+'">'
          +'<input type="hidden" name="author" value="'+esc(author)+'">'
          +'<input type="hidden" name="description" value="'+esc(desc)+'">'
          +'<input type="hidden" name="category" value="'+esc(category)+'">'
          +'<input type="hidden" name="releaseDate" value="'+esc(release)+'">'
          +'<input type="hidden" name="q" value="'+esc(query)+'">'
          +'<button class="btn btn-success btn-sm" type="submit">장바구니 담기</button></form>';
        box.insertAdjacentHTML('beforeend',
          '<div class="col-sm-6 col-lg-4 mb-4"><div class="card h-100">'
          +(d.thumbnail ? '<img class="card-img-top book-cover" alt="" src="'+esc(d.thumbnail)+'">' : '<div class="card-img-top book-cover"></div>')
          +'<div class="card-body d-flex flex-column"><h5>'+esc(title)+'</h5>'
          +'<p class="text-muted">저자: '+esc(author)+'<br>출판사: '+esc(d.publisher)+'</p>'
          +'<div class="price mb-2">'+price+'</div>'
          +'<div class="search-result-actions"><a class="btn btn-outline-primary btn-sm" target="_blank" rel="noopener" href="'+esc(d.url)+'">상세 정보</a>'
          +form+'</div></div></div></div>');
      });
      if(!o.j.meta.is_end){ page++; more.style.display='inline-block'; }
    })
    .catch(function(){ msg.textContent='서버와 통신하지 못했습니다.'; });
}
</script>
<%@ include file="footer.jsp" %>
