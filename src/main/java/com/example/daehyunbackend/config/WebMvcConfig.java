package com.example.daehyunbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${image.upload.dir}")
    private String imageUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/attach/images/ad/**")
                .addResourceLocations("file:" + imageUploadPath + "/ad/");

        registry.addResourceHandler("/attach/images/items/**")
                .addResourceLocations("file:" + imageUploadPath + "/items/");
    }

}