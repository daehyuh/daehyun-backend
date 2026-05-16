package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.TribunalCaseCafeLink;

public record TribunalCafeLinkResponse(
        Long id,
        String url
) {
    public static TribunalCafeLinkResponse from(TribunalCaseCafeLink link) {
        return new TribunalCafeLinkResponse(link.getId(), link.getUrl());
    }
}
