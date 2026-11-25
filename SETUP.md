# BS's Library 설정 안내

## 1. 데이터베이스 (Supabase)
1. [supabase.com](https://supabase.com) 에서 프로젝트를 만들고, 그때 정한 **Database Password** 를 기억해 둡니다.
2. 프로젝트 화면 위쪽 **Connect** > **Connection string** 에서 *Transaction pooler* 의 JDBC 정보를 확인합니다.
3. `src/main/webapp/WEB-INF/lib/` 에 **postgresql-42.x.jar** 를 넣습니다. (https://jdbc.postgresql.org/download/)
4. `src/main/webapp/WEB-INF/app.properties` 의 `db.url`, `db.user`, `db.password` 를 Supabase 값으로 고칩니다.
   - `db.url` : `jdbc:postgresql://aws-0-<리전>.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0`
   - `db.user` : `postgres.<프로젝트ID>`
   - 환경변수 `DB_URL`, `DB_USER`, `DB_PASSWORD` 도 지원합니다. (비밀번호는 환경변수를 권장)
5. 서버(Tomcat)를 시작하면 **테이블이 자동으로 만들어지고**, 처음 한 번만 기본 데이터가 들어갑니다.
   - 계정: `admin / 1234` (관리자), `user / 1234` (일반회원) — 비밀번호는 PBKDF2 로 해시되어 저장됩니다. 운영 전에 바꾸세요.
   - 도서: `book.txt` 의 3권
   - 쿠폰: `WELCOME10`(10%, 최대 5,000원, 가입 시 자동 지급), `BOOK3000`(3,000원, 20,000원 이상)
6. 연결에 실패하면 모든 화면 위쪽에 빨간 안내가 뜹니다.

테이블: `members`, `books`, `coupons`, `user_coupons`, `orders`, `order_items`, `cart_items` (정의는 `util/Schema.java`)

장바구니는 로그인한 회원별로 `cart_items` 에 저장되어 서버를 껐다 켜도, 다른 기기에서 로그인해도 유지됩니다. 로그인 전에 담아 둔 책은 로그인할 때 저장된 장바구니에 합쳐집니다. (비로그인 상태의 장바구니는 세션에만 있어 브라우저를 닫거나 서버를 끄면 사라집니다.)

**보안:** Supabase 는 `public` 테이블을 REST API(anon 키)로도 공개합니다. 이 앱은 시작할 때 모든 테이블에 RLS(Row Level Security)를 켜므로 API 로는 접근되지 않고, 서버의 JDBC 연결(`postgres` 계정)만 읽고 씁니다. 대시보드의 Table Editor 에서 각 테이블에 *RLS enabled* 표시가 있는지 확인하세요.

## 2. 카카오 로그인
[카카오 디벨로퍼스](https://developers.kakao.com) > 내 애플리케이션 에서:
1. **카카오 로그인** 을 *활성화* 합니다.
2. **Redirect URI** 에 `http://localhost:8080/BookMarketWeb/kakaoCallback.do` 를 등록합니다. (포트/컨텍스트가 다르면 그에 맞게)
3. **동의항목** 에서 *닉네임* 을 켭니다.
4. (선택) 보안 > Client Secret 을 켰다면 `app.properties` 의 `kakao.client.secret` 에 넣습니다.

REST API 키는 `app.properties` 의 `kakao.rest.api.key` 에 있습니다. (도서 검색과 로그인이 같은 키를 씁니다. 소스를 공개 저장소에 올린다면 키를 환경변수 `KAKAO_REST_API_KEY` 로 옮기세요.)

## 3. 쿠폰
- 회원: 상단 **쿠폰** 메뉴에서 코드를 등록하고, 주문서에서 쿠폰 한 장을 골라 사용합니다. 할인액은 서버가 다시 계산합니다.
- 관리자: 관리자 메뉴 > **쿠폰 관리** 에서 정액/정률 쿠폰을 만들고, 최소 주문금액·최대 할인액·기한·가입 시 자동 지급을 지정하며, 중지/삭제할 수 있습니다.


## 4. 독서 취향 및 맞춤 추천 (추가 기능)
- 일반 회원가입 시 관심 장르(복수 선택)와 관심 키워드를 입력할 수 있습니다.
- 기존 회원도 로그인 후 선호 장르가 비어 있으면 취향 설정 화면으로 이동합니다. 카카오 신규 회원도 첫 로그인 후 같은 화면에서 설정합니다.
- 상단의 **독서 취향** 메뉴 또는 회원정보에서 선호 장르와 키워드를 언제든 수정할 수 있습니다.
- 홈 화면은 선택한 장르와 도서 카테고리를 비교해 맞춤 도서를 표시합니다. 비회원/취향 미설정 회원에게는 기본 추천 도서를 보여줍니다.
- 카카오 책 검색 결과 카드에서 **장바구니 담기**를 누르면 해당 도서가 도서 DB에 등록되고 장바구니에 추가됩니다. 로그인 회원의 장바구니는 기존처럼 Supabase에 저장됩니다.
- `Schema.java`가 기존 `members` 테이블에 `genre_preferences`, `interest_keywords` 컬럼을 자동 추가하므로 별도 SQL 실행은 필요하지 않습니다.


## 5. 카카오 추천 및 회원정보 변경
- 도서 메뉴(`/bookList.do`) 상단에 카카오 책 검색 API 기반 추천 영역이 표시됩니다. 로그인 회원은 저장한 선호 장르와 관심 키워드를 추천 검색어로 사용하고, 비회원/취향 미설정 회원은 기본 인기 주제로 추천합니다.
- 카카오 REST API 키(`kakao.rest.api.key`)가 설정되어 있어야 추천 도서가 표시됩니다.
- 회원정보 화면에서 카카오 로그인 회원도 아이디, 이름(닉네임), 전화번호, 주소를 수정할 수 있습니다. 카카오 연결 자체는 `kakao_id`로 유지되므로 앱 내부 아이디를 바꿔도 카카오 로그인 연결은 유지됩니다.
- 이름·전화번호·주소 등 기본 회원정보 변경은 월 3회로 제한하며, 아이디는 마지막 변경 시점부터 30일이 지나야 다시 변경할 수 있습니다. 제한은 서버에서 검증하고 변경 시각은 DB에 기록됩니다. 기존 DB는 서버 시작 시 관련 컬럼이 자동 추가됩니다.
