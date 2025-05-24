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
@RequestMapping(value = "/login/oauth2", produces = "application/json")
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
    @GetMapping("/code/google")
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

}