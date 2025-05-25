package com.example.daehyunbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOrigins(
                        "https://daehyun.dev",
                        "daehyun.dev",
                        "localhost:5173",
                        "localhost:8080",
                        "api.daehyun.dev",
                        "https://api.daehyun.dev",
                        "https://daehyun.dev",
                        "https://daehyun-frontend.vercel.app/",
                        "http://localhost:5173")
                .allowedMethods("**")
                .allowCredentials(true)
                .allowedMethods("**"); // 모든 HTTP 메서드 허용
    }
}