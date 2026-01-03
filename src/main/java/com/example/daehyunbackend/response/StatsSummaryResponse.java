package com.example.daehyunbackend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatsSummaryResponse {
    private final long todayUserCount;
    private final long todayAccountCount;
    private final long totalUserCount;
    private final long totalAccountCount;
}
