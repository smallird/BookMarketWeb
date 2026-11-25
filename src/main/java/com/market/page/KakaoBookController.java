package com.market.page;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/kakaoBook.do")
public class KakaoBookController extends HttpServlet {

    // REST API 키는 WEB-INF/app.properties (kakao.rest.api.key) 에서 읽습니다.

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");

        String q = req.getParameter("q");
        if (q == null || q.isBlank()) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"검색어를 입력하세요.\"}");
            return;
        }

        int page = 1;
        try { page = Math.max(1, Math.min(50, Integer.parseInt(req.getParameter("page")))); }
        catch (Exception e) { page = 1; }

        String url = "https://dapi.kakao.com/v3/search/book?query="
                + URLEncoder.encode(q, StandardCharsets.UTF_8)
                + "&size=12&page=" + page;

        HttpRequest kakaoReq = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "KakaoAK " + com.market.util.AppConfig.get("kakao.rest.api.key", ""))
                .GET().build();

        try {
            HttpResponse<String> kakaoRes =
                    CLIENT.send(kakaoReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            res.setStatus(kakaoRes.statusCode());
            res.getWriter().write(kakaoRes.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"요청이 중단되었습니다.\"}");
        }
    }
}