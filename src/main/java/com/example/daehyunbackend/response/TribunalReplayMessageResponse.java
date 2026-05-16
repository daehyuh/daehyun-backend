package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.ReplayMessageType;
import com.example.daehyunbackend.entity.TribunalReplayMessage;

public record TribunalReplayMessageResponse(
        Long id,
        int sequenceNo,
        ReplayMessageType messageType,
        String chatType,
        String nickname,
        String jobCode,
        String jobImageUrl,
        String frameImageUrl,
        String content
) {
    public static TribunalReplayMessageResponse from(TribunalReplayMessage message) {
        return new TribunalReplayMessageResponse(
                message.getId(),
                message.getSequenceNo(),
                message.getMessageType(),
                message.getChatType(),
                message.getNickname(),
                message.getJobCode(),
                message.getJobImageUrl(),
                message.getFrameImageUrl(),
                message.getContent()
        );
    }
}
