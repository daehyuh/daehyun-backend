package com.example.daehyunbackend.response;

public record TribunalAuthorResponse(
        String nickname,
        Integer rankPoint,
        boolean anonymous,
        boolean mine
) {
    public static TribunalAuthorResponse visible(String nickname, Integer rankPoint, boolean mine) {
        return new TribunalAuthorResponse(nickname, rankPoint, false, mine);
    }

    public static TribunalAuthorResponse system(String nickname) {
        return new TribunalAuthorResponse(nickname, null, false, false);
    }

    public static TribunalAuthorResponse anonymous(int anonymousNo, boolean mine) {
        return anonymous(anonymousNo, null, mine);
    }

    public static TribunalAuthorResponse anonymous(int anonymousNo, Integer rankPoint, boolean mine) {
        return new TribunalAuthorResponse("\uC775\uBA85 " + anonymousNo, rankPoint, true, mine);
    }
}
