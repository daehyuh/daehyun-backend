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
                        "https://api.daehyun.dev",
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "https://daehyun.dev",
                        "https://daehyun-frontend.vercel.app/"
                )
                .allowedMethods("**")
                .allowCredentials(true)
                .allowedMethods("**"); // 모든 HTTP 메서드 허용
    }
}