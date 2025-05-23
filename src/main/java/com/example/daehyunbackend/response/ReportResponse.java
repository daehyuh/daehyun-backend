package com.example.daehyunbackend.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReportResponse {
    private int code;
    private String message;
    private ReportData data;
    private String error;
    private Long userId;
    private int progress;
    private String reportId;
    private int today;
    private int total;
    private int is_rewarded;

}

@Data
class ReportData {
    private int jobType;
    private Map<String, Integer> badgeMap;
    private Map<String, Integer> gamePlay;
    private Map<String, Integer> sentPost;
    private Map<String, Integer> receivedPost;
    private Map<String, Integer> yearlyPayment;
    private List<RankEntry> coplayRank;
    private List<ItemBuyEntry> itemBuyRank;
    private PaymentRank paymentRank;
    private List<RankEntry> sentPostRank;
    private List<GameModeEntry> frequentGameMode;
    private List<RankEntry> receivedPostRank;
    private int totalPayment;
}
@Data
class RankEntry {
    private int count;
    private long coplayer_id;           // or sender_id or receiver_id
    private String coplayer_nickname;   // or sender_nickname or receiver_nickname
}

@Data
class PaymentRank {
    private int rank;
    private double percentile;
}

@Data
class ItemBuyEntry {
    private int count;
    private String itemName;
    private String itemType;
    private String itemImageUrl;
}

@Data
class GameModeEntry {
    private int count;
    private String gameMode;
    private String gameModeImageUrl;
}