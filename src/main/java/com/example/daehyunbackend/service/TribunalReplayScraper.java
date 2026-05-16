package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.ReplayMessageType;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TribunalReplayScraper {
    private static final Map<String, String> REPLAY_ENDPOINTS = Map.of(
            "kr", "https://o2zj8uijbj.execute-api.ap-northeast-2.amazonaws.com/GetMafiaChat",
            "en", "https://lwlexm3imq2lvqcst4kjr6322u0acknn.lambda-url.us-east-1.on.aws/",
            "ar", "https://tmypkh5un3yriyvto7qrbenl7a0jvyje.lambda-url.me-south-1.on.aws/",
            "jp", "https://uqnenskmsmv4hl3fivzxnqe4ay0fyuke.lambda-url.ap-northeast-1.on.aws/",
            "tw", "https://aqxjqczzmryk225l25muuwfjai0dkfdq.lambda-url.ap-southeast-1.on.aws/"
    );

    private static final Set<String> NON_CHAT_TYPE_CLASSES = Set.of(
            "with-nickname",
            "chat-bubble",
            "continue",
            "display-flex",
            "flex-col"
    );

    @Value("${tribunal.replay.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${tribunal.replay.read-timeout-ms:10000}")
    private int readTimeoutMs;

    public ReplayParseResult scrape(String replayUrl) {
        ReplayAddress address = parseReplayAddress(replayUrl);
        String endpoint = REPLAY_ENDPOINTS.get(address.lang());
        if (endpoint == null) {
            throw new IllegalArgumentException("지원하지 않는 리플레이 언어입니다.");
        }

        String chatUrl = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("id", address.roomId())
                .queryParam("lang", address.lang())
                .build()
                .toUriString();

        try {
            Document document = Jsoup.connect(chatUrl)
                    .timeout(Math.max(connectTimeoutMs, readTimeoutMs))
                    .get();

            List<ReplayMessageData> messages = parseMessages(document);
            if (messages.isEmpty()) {
                throw new IllegalArgumentException("리플레이 채팅 데이터를 찾을 수 없습니다.");
            }

            List<ReplayPlayerData> players = parsePlayers(document);

            return new ReplayParseResult(
                    address.roomId(),
                    address.lang(),
                    text(document.selectFirst(".game-end-type")),
                    text(document.selectFirst(".game-type-title")),
                    parseGameDuration(document),
                    LocalDateTime.now(),
                    players,
                    messages
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("리플레이 데이터를 가져오지 못했습니다: " + e.getMessage());
        }
    }

    private ReplayAddress parseReplayAddress(String replayUrl) {
        if (isBlank(replayUrl)) {
            throw new IllegalArgumentException("리플레이 링크를 입력해주세요.");
        }

        URI uri;
        try {
            uri = URI.create(replayUrl.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("올바른 리플레이 링크가 아닙니다.");
        }

        String host = uri.getHost();
        if (host == null || !host.equalsIgnoreCase("mafia42.com")) {
            throw new IllegalArgumentException("mafia42.com 리플레이 링크만 등록할 수 있습니다.");
        }

        String[] parts = uri.getPath().split("/");
        if (parts.length < 4 || !"history".equals(parts[1])) {
            throw new IllegalArgumentException("올바른 마피아42 리플레이 경로가 아닙니다.");
        }

        String lang = parts[2].toLowerCase(Locale.ROOT);
        String roomId = parts[3];
        if (isBlank(roomId)) {
            throw new IllegalArgumentException("리플레이 ID를 찾을 수 없습니다.");
        }

        return new ReplayAddress(roomId, lang);
    }

    private List<ReplayMessageData> parseMessages(Document document) {
        List<ReplayMessageData> messages = new ArrayList<>();
        Element table = document.selectFirst("section.table");
        if (table == null) {
            return messages;
        }

        int sequence = 1;
        for (Element child : table.children()) {
            if (child.hasClass("system")) {
                String content = text(child);
                if (!isBlank(content)) {
                    messages.add(new ReplayMessageData(
                            sequence++,
                            ReplayMessageType.SYSTEM,
                            null,
                            null,
                            null,
                            null,
                            null,
                            content
                    ));
                }
                continue;
            }

            if (!child.hasClass("chat-data-container")) {
                continue;
            }

            String nickname = text(child.selectFirst(".nick-name"));
            String jobImageUrl = attr(child.selectFirst(".job-icon-img"), "src");
            String frameImageUrl = attr(child.selectFirst(".frame-img"), "src");
            String jobCode = extractJobCode(jobImageUrl);
            Elements bubbles = child.select(".chat-bubble");

            for (Element bubble : bubbles) {
                String content = normalizeBubbleText(bubble);
                if (isBlank(content)) {
                    continue;
                }

                messages.add(new ReplayMessageData(
                        sequence++,
                        ReplayMessageType.CHAT,
                        extractChatType(bubble),
                        nickname,
                        jobCode,
                        jobImageUrl,
                        frameImageUrl,
                        content
                ));
            }
        }
        return messages;
    }

    private List<ReplayPlayerData> parsePlayers(Document document) {
        List<ReplayPlayerData> players = new ArrayList<>();
        Elements items = document.select("#user-table .item");

        int order = 1;
        for (Element item : items) {
            String nickname = text(item.selectFirst(".nick-name"));
            String jobImageUrl = attr(item.selectFirst(".job-icon-img"), "src");
            String frameImageUrl = attr(item.selectFirst(".frame-img"), "src");
            String jobCode = extractJobCode(jobImageUrl);

            if (isBlank(nickname) || isBlank(jobCode)) {
                continue;
            }

            players.add(new ReplayPlayerData(
                    order++,
                    nickname,
                    jobCode,
                    jobImageUrl,
                    frameImageUrl
            ));
        }

        return players;
    }

    private String parseGameDuration(Document document) {
        for (Element info : document.select(".game-info")) {
            String text = text(info);
            if (!isBlank(text) && text.contains("시간")) {
                return text;
            }
        }
        return null;
    }

    private String extractChatType(Element bubble) {
        for (String className : bubble.classNames()) {
            if (!NON_CHAT_TYPE_CLASSES.contains(className)) {
                return className;
            }
        }
        return null;
    }

    private String extractJobCode(String jobImageUrl) {
        if (isBlank(jobImageUrl)) {
            return null;
        }

        int start = jobImageUrl.lastIndexOf("jobthumb_");
        int end = jobImageUrl.lastIndexOf('.');
        if (start < 0 || end <= start) {
            return null;
        }
        return jobImageUrl.substring(start + "jobthumb_".length(), end);
    }

    private String normalizeBubbleText(Element bubble) {
        String content = bubble.ownText();
        if (isBlank(content)) {
            content = bubble.text();
        }
        return normalizeText(content);
    }

    private String text(Element element) {
        if (element == null) {
            return null;
        }
        return normalizeText(element.text());
    }

    private String attr(Element element, String attribute) {
        if (element == null) {
            return null;
        }
        String value = element.attr(attribute);
        return isBlank(value) ? null : value;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replace('\u00A0', ' ').trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ReplayAddress(String roomId, String lang) {
    }

    public record ReplayParseResult(
            String roomId,
            String lang,
            String winnerTeam,
            String gameType,
            String gameDuration,
            LocalDateTime fetchedAt,
            List<ReplayPlayerData> players,
            List<ReplayMessageData> messages
    ) {
    }

    public record ReplayPlayerData(
            int order,
            String nickname,
            String jobCode,
            String jobImageUrl,
            String frameImageUrl
    ) {
    }

    public record ReplayMessageData(
            int sequenceNo,
            ReplayMessageType messageType,
            String chatType,
            String nickname,
            String jobCode,
            String jobImageUrl,
            String frameImageUrl,
            String content
    ) {
    }
}
