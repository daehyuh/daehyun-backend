package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.Role;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String email,
        String name,
        String provider,
        String providerId,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AdminAccountResponse> accounts
) {
}
