package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.dto.AuthResponseDTO;
import com.example.daehyunbackend.dto.MobileTokenRequest;
import com.example.daehyunbackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class AuthController {

    @Value("${frontend.url}")
    private String frontendUrl;
    @Value("${frontend.domain}")
    private String frontendDomain;
    @Value("${mobile.oauth.deep-link:com.daehyun.webview://oauth/callback}")
    private String mobileDeepLink;

    AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "👑테스트 - 로그인 콜백", tags = {"Auth"})
    @GetMapping("/login/oauth2/code/google")
    public String googleLogin(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) {
        AuthResponseDTO authResponseDTO = authService.socialLogin(code);

        String accessToken = authResponseDTO.getAccessToken();
        String refreshToken = authResponseDTO.getRefreshToken();

//        response.setHeader("Set-Cookie", "accessToken=" + accessToken + "; Path=/; Domain="+frontendDomain+"; SameSite=None; Secure;"); // httpOnly 제외됨
//        response.addHeader("Set-Cookie", "refreshToken=" + refreshToken + "; Path=/; Domain="+frontendDomain+"; SameSite=None; Secure;"); // httpOnly 제외됨
        response.setHeader("Set-Cookie", "accessToken=" + accessToken + "; Path=/; Domain="+frontendDomain+"; SameSite=None; Secure;");
        response.addHeader("Set-Cookie", "refreshToken=" + refreshToken + "; Path=/; Domain="+frontendDomain+"; SameSite=None; Secure;");


        return "redirect:"+frontendUrl;
    }

    @Operation(summary = "모바일 Google OAuth 시작", tags = {"Auth"})
    @GetMapping("/auth/mobile/start")
    public String mobileLoginStart() {
        return "redirect:" + authService.createMobileAuthorizationUrl();
    }

    @Operation(summary = "모바일 Google OAuth 콜백", tags = {"Auth"})
    @GetMapping("/login/oauth2/code/google/mobile")
    public void mobileGoogleLogin(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response
    ) throws java.io.IOException {
        try {
            String ticket = authService.createMobileLoginTicket(code, state);
            response.sendRedirect(UriComponentsBuilder.fromUriString(mobileDeepLink)
                    .queryParam("ticket", ticket)
                    .build()
                    .toUriString());
        } catch (RuntimeException exception) {
            response.sendRedirect(UriComponentsBuilder.fromUriString(mobileDeepLink)
                    .queryParam("error", "oauth_failed")
                    .build()
                    .toUriString());
        }
    }

    @Operation(summary = "모바일 로그인 티켓 교환", tags = {"Auth"})
    @ResponseBody
    @PostMapping("/auth/mobile/exchange")
    public AuthResponseDTO exchangeMobileTicket(@org.springframework.web.bind.annotation.RequestBody MobileTokenRequest request) {
        return authService.exchangeMobileTicket(request.getTicket());
    }

    @Operation(summary = "모바일 토큰 갱신", tags = {"Auth"})
    @ResponseBody
    @PostMapping("/auth/mobile/refresh")
    public AuthResponseDTO refreshMobileToken(@org.springframework.web.bind.annotation.RequestBody MobileTokenRequest request) {
        return authService.refreshMobileToken(request.getRefreshToken());
    }

    //로그아웃 (쿠키삭제)
    @Operation(summary = "👑테스트 - 로그아웃", tags = {"Auth"})
    @GetMapping("/core/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .domain(frontendDomain)
                .sameSite("None")
                .secure(true)
                .maxAge(0)
                .build();
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .domain(frontendDomain)
                .sameSite("None")
                .secure(true)
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return "redirect:"+frontendUrl;
    }

}
