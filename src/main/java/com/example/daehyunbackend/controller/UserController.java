package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.dto.UserDto;
import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.RecordRepository;
import com.example.daehyunbackend.response.ReportIdResponse;
import com.example.daehyunbackend.response.ReportResponse;
import com.example.daehyunbackend.response.UserData;
import com.example.daehyunbackend.response.UserDataResponse;
import com.example.daehyunbackend.service.AccountService;
import com.example.daehyunbackend.service.ReportService;
import com.example.daehyunbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController()
@AllArgsConstructor
public class UserController {

    private final ReportService reportService;
    private final UserService userService;
    private final AccountService accountService;
    private final RecordRepository recordRepository;

    @Operation(summary = "유저 리스트 반환", tags = {"User"})
    @GetMapping("/Users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @Operation(summary = "계정 리스트 반환 ", tags = {"User"})
    @GetMapping("/Accounts")
    public ResponseEntity<?> getAccounts() {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.findAll());
    }

    @Operation(summary = "본인 정보 조회 ", tags = {"User"})
    @GetMapping("/profile/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(UserDto.fromEntity(userService.findById(Long.parseLong(authentication.getName()))));
    }

    @Operation(summary = "유저 정보 조회 - ✅전체 접근, 접근 유저 ID 필요", tags = {"User"})
    @ApiResponse(responseCode = "200", description = "정상적으로 사용자 정보를 반환", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUsers(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(UserDto.fromEntity(userService.findById(id)));
    }

    @Operation(summary = "내 정보 수정 - ❌토큰과 UserDto 필요", tags = {"Me"})
    @ApiResponse(responseCode = "200", description = "정상적으로 사용자 정보를 수정", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))
    @PutMapping("/profile/me")
    public ResponseEntity<?> putMe(Authentication authentication, @RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.OK).body(UserDto.fromEntity(userService.updateUser(Long.valueOf(authentication.getName()), userDto)));
    }


    @Operation(summary = "유저 수 조회 - ✅전체 접근", tags = {"User"})
    @ApiResponse(responseCode = "200", description = "정상적으로 사용자 정보를 수정", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    @GetMapping("/profile/count")
    public Long count() {
        return userService.count();
    }

    @Operation(summary = "계정 동기화", tags = {"User"})
    @PostMapping("/Account/sync")
    public ResponseEntity<?> AccountSync(Authentication authentication, @RequestParam String nickname, @RequestParam String code) {
        User user = userService.findById(Long.valueOf(authentication.getName()));

        if (user.getRole() == Role.ROLE_USER) {
            // 유저가 계정을 이미 동기화한 경우
            if (accountService.existsByUser(user)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "이미 계정이 동기화되어 있습니다, 더 많은 계정을 동기화 하고싶다면 관리자에게 문의해주세요."));
            }
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "유저 조회 실패"));
        }
        System.out.println("nickname = " + nickname);

        String reportId = null;
        try {
            ReportIdResponse response = reportService.getReportId(nickname, code);

            if (response == null || response.getReportId() == null || response.getReportId().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "리포트 생성 실패"));
            }
            reportId = response.getReportId();
            System.out.println("reportId = " + reportId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "예외 발생: " + e.getMessage()));
        }

        Long userId = null;

        try {
            ReportResponse response = reportService.getUserId(reportId);

            if (response == null || response.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "유저아이디 조회 실패"));
            }
            userId = response.getUserId();
            System.out.println("userId = " + userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "예외 발생: " + e.getMessage()));
        }


        Account account = Account.builder()
                .accountId(userId)
                .user(user)
                .createdAt(LocalDateTime.now())
                .secretKey(code)
                .build();

        accountService.save(account);

        UserDataResponse userDataResponse = null;
        try {
            UserDataResponse response = reportService.getUserData(userId);
            if (response == null ) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "유저 데이터 조회 실패"));
            }
            userDataResponse = response;

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.daehyunbackend.response.ApiResponse<>(false, null, "예외 발생: " + e.getMessage()));
        }

        UserData userData =  userDataResponse.getUserData();
        Record record = Record.fromEntity(userData);
        record.setAccount(account);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new com.example.daehyunbackend.response.ApiResponse<>(true, recordRepository.save(record), "계정 동기화 성공"));

    }
}