package com.example.daehyunbackend.support;

import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

public final class MafiaJobNameResolver {
    private static final Map<String, String> JOB_NAMES = Map.ofEntries(
            entry("mafia", "마피아"),
            entry("spy", "스파이"),
            entry("beastman", "짐승인간"),
            entry("madam", "마담"),
            entry("thief", "도둑"),
            entry("witch", "마녀"),
            entry("scientist", "과학자"),
            entry("swindler", "사기꾼"),
            entry("hitman", "청부업자"),
            entry("cleaner", "청소부"),
            entry("citizen", "시민"),
            entry("police", "경찰"),
            entry("doctor", "의사"),
            entry("soldier", "군인"),
            entry("politician", "정치인"),
            entry("terrorist", "테러리스트"),
            entry("reporter", "기자"),
            entry("journalist", "기자"),
            entry("detective", "사립탐정"),
            entry("gangster", "건달"),
            entry("shaman", "영매"),
            entry("priest", "성직자"),
            entry("magician", "마술사"),
            entry("nurse", "간호사"),
            entry("judge", "판사"),
            entry("prophet", "예언자"),
            entry("hacker", "해커"),
            entry("mentalist", "심리학자"),
            entry("fortuneteller", "점쟁이"),
            entry("mercenary", "용병"),
            entry("vigilante", "자경단원"),
            entry("official", "공무원"),
            entry("official_small", "공무원"),
            entry("official_big", "공무원"),
            entry("cultleader", "교주"),
            entry("cult_leader", "교주"),
            entry("fanatic", "광신도"),
            entry("ghoul", "구울"),
            entry("paparazzi", "파파라치")
    );

    private MafiaJobNameResolver() {
    }

    public static String resolve(String jobCode) {
        String normalized = normalize(jobCode);
        if (normalized == null) {
            return null;
        }
        return JOB_NAMES.getOrDefault(normalized, fallbackName(normalized));
    }

    private static String normalize(String jobCode) {
        if (jobCode == null || jobCode.isBlank()) {
            return null;
        }

        String normalized = jobCode.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        if (normalized.startsWith("jobthumb_")) {
            normalized = normalized.substring("jobthumb_".length());
        }

        int extensionIndex = normalized.lastIndexOf('.');
        if (extensionIndex > 0) {
            normalized = normalized.substring(0, extensionIndex);
        }

        return normalized.isBlank() ? null : normalized;
    }

    private static String fallbackName(String normalized) {
        boolean hasNonAscii = normalized.chars().anyMatch(codePoint -> codePoint > 127);
        return hasNonAscii ? normalized : "알 수 없는 직업";
    }
}
