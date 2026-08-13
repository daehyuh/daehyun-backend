package com.example.daehyunbackend.response;

public record AdminAccountActionResponse(
        Long userId,
        Long accountId,
        boolean dataDeleted,
        long deletedRecords,
        long deletedSnapshots,
        long deletedDailyBaselines,
        long deletedGuestMappings
) {
}
