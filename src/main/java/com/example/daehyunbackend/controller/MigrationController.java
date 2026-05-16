package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.response.ApiResponse;
import com.example.daehyunbackend.response.LegacyMigrationStatusResponse;
import com.example.daehyunbackend.service.LegacyDataMigrationService;
import com.example.daehyunbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/migration")
public class MigrationController {
    private final LegacyDataMigrationService legacyDataMigrationService;
    private final UserService userService;

    @GetMapping("/legacy-records")
    public ResponseEntity<ApiResponse<LegacyMigrationStatusResponse>> getLegacyRecordMigrationStatus(
            Authentication authentication
    ) {
        ResponseEntity<ApiResponse<LegacyMigrationStatusResponse>> forbidden = requireAdmin(authentication);
        if (forbidden != null) {
            return forbidden;
        }

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                legacyDataMigrationService.getStatus(),
                "ok"
        ));
    }

    @PostMapping("/legacy-records/run")
    public ResponseEntity<ApiResponse<LegacyMigrationStatusResponse>> runLegacyRecordMigrationBatch(
            Authentication authentication
    ) {
        ResponseEntity<ApiResponse<LegacyMigrationStatusResponse>> forbidden = requireAdmin(authentication);
        if (forbidden != null) {
            return forbidden;
        }

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                legacyDataMigrationService.migrateNextBatch(),
                "ok"
        ));
    }

    private ResponseEntity<ApiResponse<LegacyMigrationStatusResponse>> requireAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, null, "unauthorized"));
        }

        User user = userService.findById(Long.parseLong(authentication.getName()));
        if (user.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, null, "forbidden"));
        }
        return null;
    }
}
