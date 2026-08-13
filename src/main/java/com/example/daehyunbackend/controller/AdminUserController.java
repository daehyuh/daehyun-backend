package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.response.AdminAccountActionResponse;
import com.example.daehyunbackend.response.AdminUserPageResponse;
import com.example.daehyunbackend.response.ApiResponse;
import com.example.daehyunbackend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserPageResponse>> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, adminUserService.findUsers(page, size, query), "OK"));
    }

    @DeleteMapping("/{userId}/accounts/{accountRecordId}")
    public ResponseEntity<ApiResponse<AdminAccountActionResponse>> unlinkAccount(
            @PathVariable Long userId,
            @PathVariable Long accountRecordId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                adminUserService.unlinkAccount(userId, accountRecordId),
                "Account unlinked"
        ));
    }

    @DeleteMapping("/{userId}/accounts/{accountRecordId}/data")
    public ResponseEntity<ApiResponse<AdminAccountActionResponse>> deleteAccountData(
            @PathVariable Long userId,
            @PathVariable Long accountRecordId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                adminUserService.deleteAccountData(userId, accountRecordId),
                "Account and linked data deleted"
        ));
    }
}
