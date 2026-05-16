package com.example.daehyunbackend.response;

public record TribunalAuthorResponse(
        String nickname,
        boolean anonymous,
        boolean mine
) {
    public static TribunalAuthorResponse visible(String nickname, boolean mine) {
        return new TribunalAuthorResponse(nickname, false, mine);
    }

    public static TribunalAuthorResponse anonymous(boolean mine) {
        return new TribunalAuthorResponse("익명", true, mine);
    }
}
