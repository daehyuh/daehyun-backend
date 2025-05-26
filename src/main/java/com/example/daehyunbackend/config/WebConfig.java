package com.example.daehyunbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "https://daehyun.dev", "http://localhost:8080", "https://api.daehyun.dev", "https://대현.com") // 정확한 Origin 명시
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}