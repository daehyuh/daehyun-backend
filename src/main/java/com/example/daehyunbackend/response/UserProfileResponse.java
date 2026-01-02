package com.example.daehyunbackend.response;

import com.example.daehyunbackend.dto.UserDto;
import com.example.daehyunbackend.entity.Record;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private UserDto user;
    private Record record;
    private String nicknameColor;
    private Integer nicknameRank;
    private String guildBackgroundColor;
    private Integer guildBackgroundRank;
    private Integer todayWinCount;
    private Integer todayLoseCount;
    private Integer todayTotalCount;
    private Boolean todayLimitExceeded;
}
