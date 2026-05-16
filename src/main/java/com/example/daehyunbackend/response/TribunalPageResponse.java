package com.example.daehyunbackend.response;

import java.util.List;

public record TribunalPageResponse<T>(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<T> content
) {
}
