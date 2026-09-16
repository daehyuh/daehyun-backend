package com.example.daehyunbackend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MobileTokenRequest {
    private String ticket;
    private String refreshToken;
}
