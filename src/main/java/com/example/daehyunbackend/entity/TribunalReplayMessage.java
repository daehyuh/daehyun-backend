package com.example.daehyunbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tribunal_replay_message",
        indexes = {
                @Index(name = "idx_tribunal_replay_message_case_seq", columnList = "case_id, sequence_no"),
                @Index(name = "idx_tribunal_replay_message_nickname", columnList = "nickname")
        }
)
public class TribunalReplayMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private TribunalCase tribunalCase;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ReplayMessageType messageType;

    @Column(name = "chat_type", length = 50)
    private String chatType;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "job_code", length = 100)
    private String jobCode;

    @Column(name = "job_image_url", length = 1000)
    private String jobImageUrl;

    @Column(name = "frame_image_url", length = 1000)
    private String frameImageUrl;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    public static TribunalReplayMessage create(
            TribunalCase tribunalCase,
            int sequenceNo,
            ReplayMessageType messageType,
            String chatType,
            String nickname,
            String jobCode,
            String jobImageUrl,
            String frameImageUrl,
            String content
    ) {
        TribunalReplayMessage message = new TribunalReplayMessage();
        message.tribunalCase = tribunalCase;
        message.sequenceNo = sequenceNo;
        message.messageType = messageType;
        message.chatType = chatType;
        message.nickname = nickname;
        message.jobCode = jobCode;
        message.jobImageUrl = jobImageUrl;
        message.frameImageUrl = frameImageUrl;
        message.content = content;
        return message;
    }
}
