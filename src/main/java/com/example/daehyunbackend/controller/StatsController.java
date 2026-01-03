package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.response.ApiResponse;
import com.example.daehyunbackend.response.StatsSummaryResponse;
import com.example.daehyunbackend.service.AccountService;
import com.example.daehyunbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatsController {

    private final UserService userService;
    private final AccountService accountService;

    @Operation(summary = "오늘/전체 가입 및 계정 수 요약", tags = {"Stats"})
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StatsSummaryResponse>> getSummary() {
        StatsSummaryResponse data = new StatsSummaryResponse(
                userService.countToday(),
                accountService.countToday(),
                userService.count(),
                accountService.count()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, data, "ok"));
    }
}
