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
//                // TODO 프로덕션 환경 자동 감지
//                /* DEV */
                "http://localhost", // backend
                "http://localhost:5173", // frontend
                     "https://daehyuh.dev", // frontend (React)
                    "https://api.daehyuh.dev", // frontend (React)
//                /* PRODUCTION */
                "https://hufsnc.com", // backend
                "https://대현.com" // frontend
            )
                .allowedOrigins("*")
                .allowedMethods("*"); // 필요한 메서드 추가
    }
}