package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.dto.TribunalCaseCreateRequest;
import com.example.daehyunbackend.dto.TribunalCommentRequest;
import com.example.daehyunbackend.dto.TribunalReplayPreviewRequest;
import com.example.daehyunbackend.dto.TribunalVoteRequest;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.response.ApiResponse;
import com.example.daehyunbackend.response.TribunalCaseDetailResponse;
import com.example.daehyunbackend.response.TribunalCaseSummaryResponse;
import com.example.daehyunbackend.response.TribunalCommentResponse;
import com.example.daehyunbackend.response.TribunalPageResponse;
import com.example.daehyunbackend.response.TribunalReplayPreviewResponse;
import com.example.daehyunbackend.response.TribunalVoteSummaryResponse;
import com.example.daehyunbackend.service.TribunalService;
import com.example.daehyunbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tribunal/cases")
public class TribunalController {
    private final TribunalService tribunalService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<TribunalCaseDetailResponse>> createCase(
            Authentication authentication,
            @RequestBody TribunalCaseCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        tribunalService.createCase(request, currentUser(authentication)),
                        "사건 등록 성공"
                ));
    }

    @PostMapping("/replay/preview")
    public ResponseEntity<ApiResponse<TribunalReplayPreviewResponse>> previewReplay(
            Authentication authentication,
            @RequestBody TribunalReplayPreviewRequest request
    ) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                tribunalService.previewReplay(request, user),
                "리플레이 확인 성공"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<TribunalPageResponse<TribunalCaseSummaryResponse>>> getCases(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                tribunalService.getCases(page, size, optionalUser(authentication)),
                "사건 목록 조회 성공"
        ));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<TribunalCaseDetailResponse>> getCase(
            Authentication authentication,
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                tribunalService.getCase(caseId, optionalUser(authentication)),
                "사건 조회 성공"
        ));
    }

    @DeleteMapping("/{caseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCase(
            Authentication authentication,
            @PathVariable Long caseId
    ) {
        tribunalService.deleteCase(caseId, currentUser(authentication));
        return ResponseEntity.ok(new ApiResponse<>(true, null, "사건 삭제 성공"));
    }

    @PostMapping("/{caseId}/votes")
    public ResponseEntity<ApiResponse<TribunalVoteSummaryResponse>> vote(
            Authentication authentication,
            @PathVariable Long caseId,
            @RequestBody TribunalVoteRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                tribunalService.vote(caseId, request, currentUser(authentication)),
                "투표 반영 성공"
        ));
    }

    @PostMapping("/{caseId}/comments")
    public ResponseEntity<ApiResponse<TribunalCommentResponse>> addComment(
            Authentication authentication,
            @PathVariable Long caseId,
            @RequestBody TribunalCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        tribunalService.addComment(caseId, request, currentUser(authentication)),
                        "댓글 등록 성공"
                ));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<TribunalCommentResponse>> updateComment(
            Authentication authentication,
            @PathVariable Long commentId,
            @RequestBody TribunalCommentRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                tribunalService.updateComment(commentId, request, currentUser(authentication)),
                "댓글 수정 성공"
        ));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            Authentication authentication,
            @PathVariable Long commentId
    ) {
        tribunalService.deleteComment(commentId, currentUser(authentication));
        return ResponseEntity.ok(new ApiResponse<>(true, null, "댓글 삭제 성공"));
    }

    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<ApiResponse<TribunalCommentResponse>> toggleCommentLike(
            Authentication authentication,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                tribunalService.toggleCommentLike(commentId, currentUser(authentication)),
                "댓글 인정 상태 변경 성공"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, null, e.getMessage()));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return userService.findById(Long.parseLong(authentication.getName()));
    }

    private User optionalUser(Authentication authentication) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null) {
            return null;
        }

        try {
            return userService.findById(Long.parseLong(authentication.getName()));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
