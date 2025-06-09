package com.example.daehyunbackend.dto;

import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GuestDto {
    @Schema(description = "닉네임", example = "대현닷컴")
    private String nickName;
}
