package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.TribunalVerdict;

public record TribunalVoteSummaryResponse(
        long guiltyCount,
        long notGuiltyCount,
        TribunalVerdict myVerdict
) {
}
