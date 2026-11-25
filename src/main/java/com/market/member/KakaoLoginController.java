package com.market.member;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.market.util.AppConfig;
import com.market.util.Json;

/**
 * 카카오 로그인 (인가 코드 방식)
 *   /kakaoLogin.do    : 카카오 로그인 창으로 보냄
 *   /kakaoCallback.do : 카카오가 돌려보내는 주소. 토큰 교환 → 사용자 정보 조회 → 가입/로그인
 */
@WebServlet(urlPatterns = {"/kakaoLogin.do", "/kakaoCallback.do"})
public class KakaoLoginController extends HttpServlet {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getServletPath().equals("/kakaoLogin.do")) start(req, res);
        else callback(req, res);
    }

    private void start(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String state = new BigInteger(130, new SecureRandom()).toString(32);
        req.getSession().setAttribute("kakaoState", state);
        String url = "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + enc(AppConfig.get("kakao.rest.api.key", ""))
                + "&redirect_uri=" + enc(redirectUri(req))
                // 매번 카카오계정 로그인 화면을 거쳐 사용자 인증을 다시 수행
                + "&prompt=login"
                + "&state=" + state;
        res.sendRedirect(url);
    }

    private void callback(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String ctx = req.getContextPath();
        HttpSession s = req.getSession();
        String expected = (String) s.getAttribute("kakaoState");
        s.removeAttribute("kakaoState");
        String code = req.getParameter("code");

        if (req.getParameter("error") != null || code == null) { res.sendRedirect(ctx + "/login.jsp?error=kakaoCancel"); return; }
        if (expected == null || !expected.equals(req.getParameter("state"))) { res.sendRedirect(ctx + "/login.jsp?error=kakao"); return; }

        try {
            // 1) 인가 코드 → 액세스 토큰
            StringBuilder form = new StringBuilder("grant_type=authorization_code")
                    .append("&client_id=").append(enc(AppConfig.get("kakao.rest.api.key", "")))
                    .append("&redirect_uri=").append(enc(redirectUri(req)))
                    .append("&code=").append(enc(code));
            String secret = AppConfig.get("kakao.client.secret", "");
            if (!secret.isEmpty()) form.append("&client_secret=").append(enc(secret));

            HttpResponse<String> tokenRes = HTTP.send(HttpRequest.newBuilder(URI.create("https://kauth.kakao.com/oauth/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString())).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String accessToken = Json.str(tokenRes.body(), "access_token");
            if (accessToken == null) { log("kakao token error: " + tokenRes.body()); res.sendRedirect(ctx + "/login.jsp?error=kakao"); return; }

            // 2) 액세스 토큰 → 사용자 정보
            HttpResponse<String> meRes = HTTP.send(HttpRequest.newBuilder(URI.create("https://kapi.kakao.com/v2/user/me"))
                    .header("Authorization", "Bearer " + accessToken).GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Long kakaoNo = Json.num(meRes.body(), "id");        // 응답 맨 앞의 id 가 회원번호
            if (kakaoNo == null) { log("kakao me error: " + meRes.body()); res.sendRedirect(ctx + "/login.jsp?error=kakao"); return; }
            String nickname = Json.str(meRes.body(), "nickname");
            if (nickname == null || nickname.isBlank()) nickname = "카카오회원";

            // 3) 이미 가입된 카카오 회원이면 로그인, 처음이면 가입 후 로그인
            String kakaoId = String.valueOf(kakaoNo);
            Member m = MemberDao.findByKakaoId(kakaoId);
            boolean first = (m == null);
            if (first) {
                MemberDao.register("kakao_" + kakaoId, null, nickname, "", "", "일반회원", kakaoId);
                m = MemberDao.findByKakaoId(kakaoId);
            }
            Auth.login(req, m, "kakao");
            if (first) {
                Auth.flash(req.getSession(), "success", nickname + "님, 가입을 환영합니다. 신규 가입 쿠폰이 지급되었어요.");
                res.sendRedirect(ctx + "/preferences.jsp");
            } else if (m.getGenrePreferences() == null || m.getGenrePreferences().isBlank()) {
                res.sendRedirect(ctx + "/preferences.jsp");
            } else res.sendRedirect(ctx + "/index.jsp");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            res.sendRedirect(ctx + "/login.jsp?error=kakao");
        } catch (Exception e) {
            log("kakao login failed", e);
            res.sendRedirect(ctx + "/login.jsp?error=kakao");
        }
    }

    static String redirectUri(HttpServletRequest req) {
        String configured = AppConfig.get("kakao.redirect.uri", "");
        if (!configured.isEmpty()) return configured;
        int port = req.getServerPort();
        boolean std = (req.getScheme().equals("http") && port == 80) || (req.getScheme().equals("https") && port == 443);
        return req.getScheme() + "://" + req.getServerName() + (std ? "" : ":" + port) + req.getContextPath() + "/kakaoCallback.do";
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
