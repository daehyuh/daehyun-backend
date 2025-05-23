package com.example.daehyunbackend.dto;

import com.example.daehyunbackend.entity.Auth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;


    public static AuthResponseDTO fromEntity(Auth entity) {
        return AuthResponseDTO.builder()
                .accessToken(entity.getAccessToken())
                .refreshToken(entity.getRefreshToken())
                .build();
    }

}