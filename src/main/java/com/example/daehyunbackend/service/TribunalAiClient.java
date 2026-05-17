package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.TribunalVerdict;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TribunalAiClient {
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${tribunal.ai.base-url:http://127.0.0.1:8010}")
    private String baseUrl;

    @Value("${tribunal.ai.api-key:}")
    private String apiKey;

    @Value("${tribunal.ai.connect-timeout-ms:5000}")
    private long connectTimeoutMs;

    @Value("${tribunal.ai.read-timeout-ms:120000}")
    private long readTimeoutMs;

    public TribunalAiReviewClientResponse review(TribunalAiReviewClientRequest request) {
        RestTemplate restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-Daehyun-AI-Key", apiKey);
        }

        ResponseEntity<TribunalAiReviewClientResponse> response = restTemplate.postForEntity(
                reviewUrl(),
                new HttpEntity<>(request, headers),
                TribunalAiReviewClientResponse.class
        );

        TribunalAiReviewClientResponse body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("AI server returned an empty response.");
        }
        return body;
    }

    private String reviewUrl() {
        return baseUrl.replaceAll("/+$", "") + "/v1/tribunal/reviews";
    }

    public record TribunalAiReviewClientRequest(
            Long caseId,
            String replayUrl,
            String replayRoomId,
            String replayLang,
            String playerNickname,
            String playerPick,
            String playerPickName,
            String description,
            String winnerTeam,
            String gameType,
            String gameDuration,
            List<ReplayMessageRequest> replayMessages
    ) {
    }

    public record ReplayMessageRequest(
            Long id,
            int sequenceNo,
            String messageType,
            String chatType,
            String nickname,
            String jobCode,
            String content
    ) {
    }

    public record TribunalAiReviewClientResponse(
            Long caseId,
            String playerNickname,
            String playerPick,
            String playerPickName,
            TribunalVerdict verdict,
            Integer score,
            String grade,
            Integer teamAlignment,
            Double confidence,
            String summary,
            List<String> goodPoints,
            List<String> badPoints,
            List<TimelineFinding> timeline,
            List<String> recommendations,
            String model,
            Object raw
    ) {
    }

    public record TimelineFinding(
            Integer sequenceNo,
            String speaker,
            String quote,
            String judgment,
            String reason
    ) {
    }
}
