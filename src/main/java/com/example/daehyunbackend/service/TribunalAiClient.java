package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.TribunalVerdict;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TribunalAiClient {
    private final ObjectMapper objectMapper;

    @Value("${tribunal.ai.base-url:http://127.0.0.1:8010}")
    private String baseUrl;

    @Value("${tribunal.ai.api-key:}")
    private String apiKey;

    @Value("${tribunal.ai.connect-timeout-ms:5000}")
    private long connectTimeoutMs;

    @Value("${tribunal.ai.read-timeout-ms:120000}")
    private long readTimeoutMs;

    public TribunalAiReviewClientResponse review(TribunalAiReviewClientRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI review request must not be null.");
        }

        String payload = writePayload(request);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(reviewUrl()))
                .timeout(Duration.ofMillis(readTimeoutMs))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        if (apiKey != null && !apiKey.isBlank()) {
            requestBuilder.header("X-Daehyun-AI-Key", apiKey);
        }

        HttpResponse<String> response = send(client, requestBuilder.build());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "AI server returned " + response.statusCode() + ": " + limit(response.body(), 500)
            );
        }

        return readResponse(response.body());
    }

    private String reviewUrl() {
        return baseUrl.replaceAll("/+$", "") + "/v1/tribunal/reviews";
    }

    private String writePayload(TribunalAiReviewClientRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize AI review request.", e);
        }
    }

    private HttpResponse<String> send(HttpClient client, HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI review request was interrupted.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call AI review server.", e);
        }
    }

    private TribunalAiReviewClientResponse readResponse(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("AI server returned an empty response.");
        }
        try {
            return objectMapper.readValue(body, TribunalAiReviewClientResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI review response: " + limit(body, 500), e);
        }
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
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
