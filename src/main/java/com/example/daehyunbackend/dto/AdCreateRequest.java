package com.example.daehyunbackend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class AdCreateRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String href;
    private MultipartFile image;
}