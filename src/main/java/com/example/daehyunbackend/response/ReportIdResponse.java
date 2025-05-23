package com.example.daehyunbackend.response;

import lombok.Data;

@Data
public class ReportIdResponse {
    private int code;
    private String message;
    private String reportId;
}