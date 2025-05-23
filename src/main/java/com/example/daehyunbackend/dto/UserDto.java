package com.example.daehyunbackend.dto;

import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDto {

    @Schema(description = "유저 ID", example = "1")
    private Long id;

    @Schema(description = "소셜 제공자 고유 ID", example = "abc12345")
    private String providerId;

    @Schema(description = "소셜 로그인 제공자", example = "google")
    private String provider;

    @Schema(description = "유저 이름", example = "강대현")
    private String name;

    @Schema(description = "이메일", example = "daehyun@example.com")
    private String email;

    @Schema(description = "아바타 URL", example = "https://...")
    private String avatarUrl;

    @Schema(description = "생성 일시", example = "2025-05-23T23:10:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-05-23T23:10:00")
    private LocalDateTime updatedAt;

    @Schema(description = "유저 권한", example = "ROLE_USER")
    private Role role;

    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getProviderId(),
                user.getProvider(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRole()
        );
    }
}
