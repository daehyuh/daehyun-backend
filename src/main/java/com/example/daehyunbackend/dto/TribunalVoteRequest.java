package com.example.daehyunbackend.dto;

import com.example.daehyunbackend.entity.TribunalVerdict;

public record TribunalVoteRequest(
        TribunalVerdict verdict
) {
}
