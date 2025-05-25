package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.dto.AuthResponseDTO;
import com.example.daehyunbackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Value("${frontend.url}")
    private String frontendUrl;
    @Value("${frontend.domain}")
    private String frontendDomain;

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

    //로그아웃 (쿠키삭제)
    @Operation(summary = "👑테스트 - 로그아웃", tags = {"Auth"})
    @GetMapping("/logout")
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