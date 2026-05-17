package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.TribunalAiReview;
import com.example.daehyunbackend.entity.TribunalAiReviewStatus;
import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalCaseComment;
import com.example.daehyunbackend.entity.TribunalCommentType;
import com.example.daehyunbackend.entity.TribunalReplayMessage;
import com.example.daehyunbackend.entity.TribunalVerdict;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.event.TribunalAiReviewRequestedEvent;
import com.example.daehyunbackend.repository.TribunalAiReviewRepository;
import com.example.daehyunbackend.repository.TribunalCaseCommentRepository;
import com.example.daehyunbackend.repository.TribunalCaseRepository;
import com.example.daehyunbackend.repository.TribunalReplayMessageRepository;
import com.example.daehyunbackend.repository.UserRepository;
import com.example.daehyunbackend.support.MafiaJobNameResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TribunalAiReviewService {
    private static final String AI_PROVIDER_ID = "system:tribunal-ai";
    private static final String AI_PROVIDER = "system";
    private static final String AI_NAME = "AI 판결";
    private static final String AI_EMAIL = "tribunal-ai@system.local";
    private static final int COMMENT_LIMIT = 4000;

    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final TribunalAiClient tribunalAiClient;
    private final TribunalAiReviewRepository aiReviewRepository;
    private final TribunalCaseRepository tribunalCaseRepository;
    private final TribunalReplayMessageRepository replayMessageRepository;
    private final TribunalCaseCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Value("${tribunal.ai.enabled:true}")
    private boolean enabled;

    public void enqueueReview(TribunalCase tribunalCase) {
        if (!enabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        aiReviewRepository.findByTribunalCase(tribunalCase)
                .orElseGet(() -> aiReviewRepository.save(TribunalAiReview.pending(tribunalCase, now)));
        eventPublisher.publishEvent(new TribunalAiReviewRequestedEvent(tribunalCase.getId()));
    }

    @Async("tribunalAiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewRequested(TribunalAiReviewRequestedEvent event) {
        if (!enabled) {
            return;
        }

        ReviewWork work = transactionTemplate.execute(status -> prepareWork(event.caseId(), status));
        if (work == null) {
            return;
        }

        try {
            TribunalAiClient.TribunalAiReviewClientResponse response = tribunalAiClient.review(work.request());
            transactionTemplate.executeWithoutResult(status -> saveSuccess(work.caseId(), response));
        } catch (Exception e) {
            log.warn("Tribunal AI review failed. caseId={}", work.caseId(), e);
            transactionTemplate.executeWithoutResult(status -> saveFailure(work.caseId(), e));
        }
    }

    private ReviewWork prepareWork(Long caseId, TransactionStatus status) {
        TribunalCase tribunalCase = tribunalCaseRepository.findById(caseId).orElse(null);
        if (tribunalCase == null) {
            return null;
        }

        TribunalAiReview review = aiReviewRepository.findByTribunalCase(tribunalCase)
                .orElseGet(() -> aiReviewRepository.save(TribunalAiReview.pending(tribunalCase, LocalDateTime.now())));
        if (review.getStatus() == TribunalAiReviewStatus.RUNNING
                || review.getStatus() == TribunalAiReviewStatus.SUCCEEDED) {
            return null;
        }

        review.markRunning(LocalDateTime.now());
        List<TribunalReplayMessage> replayMessages = replayMessageRepository.findByTribunalCaseOrderBySequenceNoAsc(
                tribunalCase
        );

        return new ReviewWork(
                caseId,
                new TribunalAiClient.TribunalAiReviewClientRequest(
                        tribunalCase.getId(),
                        tribunalCase.getReplayUrl(),
                        tribunalCase.getReplayRoomId(),
                        tribunalCase.getReplayLang(),
                        tribunalCase.getPlayerNickname(),
                        tribunalCase.getPlayerPick(),
                        MafiaJobNameResolver.resolve(tribunalCase.getPlayerPick()),
                        tribunalCase.getDescription(),
                        tribunalCase.getWinnerTeam(),
                        tribunalCase.getGameType(),
                        tribunalCase.getGameDuration(),
                        replayMessages.stream()
                                .map(message -> new TribunalAiClient.ReplayMessageRequest(
                                        message.getId(),
                                        message.getSequenceNo(),
                                        message.getMessageType().name(),
                                        message.getChatType(),
                                        message.getNickname(),
                                        message.getJobCode(),
                                        message.getContent()
                                ))
                                .toList()
                )
        );
    }

    private void saveSuccess(Long caseId, TribunalAiClient.TribunalAiReviewClientResponse response) {
        TribunalCase tribunalCase = tribunalCaseRepository.findById(caseId).orElse(null);
        if (tribunalCase == null) {
            return;
        }

        TribunalAiReview review = aiReviewRepository.findByTribunalCase(tribunalCase)
                .orElseGet(() -> aiReviewRepository.save(TribunalAiReview.pending(tribunalCase, LocalDateTime.now())));
        TribunalVerdict verdict = response.verdict() == null ? inferVerdict(response) : response.verdict();
        LocalDateTime now = LocalDateTime.now();

        review.markSucceeded(
                verdict,
                response.score(),
                response.grade(),
                response.teamAlignment(),
                response.confidence(),
                response.summary(),
                writeJson(response.goodPoints()),
                writeJson(response.badPoints()),
                writeJson(response.timeline()),
                writeJson(response.recommendations()),
                response.model(),
                writeJson(response.raw() == null ? response : response.raw()),
                now
        );

        saveAiComment(tribunalCase, verdict, response, now);
    }

    private void saveFailure(Long caseId, Exception e) {
        TribunalCase tribunalCase = tribunalCaseRepository.findById(caseId).orElse(null);
        if (tribunalCase == null) {
            return;
        }

        TribunalAiReview review = aiReviewRepository.findByTribunalCase(tribunalCase)
                .orElseGet(() -> aiReviewRepository.save(TribunalAiReview.pending(tribunalCase, LocalDateTime.now())));
        review.markFailed(limit(errorMessage(e), 1000), LocalDateTime.now());
    }

    private void saveAiComment(
            TribunalCase tribunalCase,
            TribunalVerdict verdict,
            TribunalAiClient.TribunalAiReviewClientResponse response,
            LocalDateTime now
    ) {
        User aiUser = findOrCreateAiUser(now);
        String content = buildAiComment(verdict, response);

        commentRepository.findFirstByTribunalCaseAndCommentTypeOrderByCreatedAtAsc(
                        tribunalCase,
                        TribunalCommentType.AI_JUDGMENT
                )
                .ifPresentOrElse(comment -> {
                    comment.setAuthor(aiUser);
                    comment.setParent(null);
                    comment.setAnonymous(false);
                    comment.setDeleted(false);
                    comment.setContent(content);
                    comment.setUpdatedAt(now);
                }, () -> commentRepository.save(TribunalCaseComment.createAiJudgment(
                        tribunalCase,
                        aiUser,
                        content,
                        now
                )));
    }

    private User findOrCreateAiUser(LocalDateTime now) {
        return userRepository.findByProviderId(AI_PROVIDER_ID)
                .orElseGet(() -> userRepository.save(User.builder()
                        .providerId(AI_PROVIDER_ID)
                        .provider(AI_PROVIDER)
                        .name(AI_NAME)
                        .email(AI_EMAIL)
                        .role(Role.ROLE_USER)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()));
    }

    private TribunalVerdict inferVerdict(TribunalAiClient.TribunalAiReviewClientResponse response) {
        Integer score = response.score();
        Integer teamAlignment = response.teamAlignment();
        String grade = response.grade();

        boolean guiltyGrade = "POOR".equalsIgnoreCase(grade) || "HARMFUL".equalsIgnoreCase(grade);
        boolean guiltyScore = score != null && score < 50;
        boolean guiltyAlignment = teamAlignment != null && teamAlignment < 0;
        return guiltyGrade || guiltyScore || guiltyAlignment
                ? TribunalVerdict.GUILTY
                : TribunalVerdict.NOT_GUILTY;
    }

    private String buildAiComment(TribunalVerdict verdict, TribunalAiClient.TribunalAiReviewClientResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append("[AI 판결] ").append(verdict == TribunalVerdict.GUILTY ? "유죄" : "무죄").append("\n");
        appendMetric(builder, "점수", response.score() == null ? null : response.score() + "/100");
        appendMetric(builder, "팀 기여도", response.teamAlignment() == null ? null : response.teamAlignment() + "/100");
        appendMetric(builder, "신뢰도", response.confidence() == null ? null : Math.round(response.confidence() * 100) + "%");
        appendMetric(builder, "등급", response.grade());
        if (response.summary() != null && !response.summary().isBlank()) {
            builder.append("\n").append(response.summary().trim()).append("\n");
        }
        appendList(builder, "좋았던 점", response.goodPoints());
        appendList(builder, "아쉬운 점", response.badPoints());
        appendList(builder, "개선 제안", response.recommendations());
        if (response.model() != null && !response.model().isBlank()) {
            builder.append("\n모델: ").append(response.model().trim());
        }
        return limit(builder.toString().trim(), COMMENT_LIMIT);
    }

    private void appendMetric(StringBuilder builder, String label, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(label).append(": ").append(value).append("\n");
        }
    }

    private void appendList(StringBuilder builder, String title, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        builder.append("\n").append(title).append("\n");
        values.stream()
                .filter(value -> value != null && !value.isBlank())
                .limit(5)
                .forEach(value -> builder.append("- ").append(value.trim()).append("\n"));
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String errorMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private record ReviewWork(Long caseId, TribunalAiClient.TribunalAiReviewClientRequest request) {
    }
}
