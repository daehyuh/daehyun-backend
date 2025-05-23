package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.RecordRepository;
import com.example.daehyunbackend.response.ApiResponse;
import com.example.daehyunbackend.response.UserData;
import com.example.daehyunbackend.response.UserDataResponse;
import com.example.daehyunbackend.service.AccountService;
import com.example.daehyunbackend.service.ReportService;
import com.example.daehyunbackend.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/core")
public class CoreController {
    private final ReportService reportService;
    private final UserService userService;
    private final AccountService accountService;
    private final RecordRepository recordRepository;


    @Operation(summary = "동접 체크 - ✅전체 접근", tags = {"Core"})
    @GetMapping("/getChannel")
    public ResponseEntity<ApiResponse<?>> getChannel() {
        String url = "https://mafia42.com/php/channels/channel_ko.php";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
        );

        List<Map<String, Object>> channelData = new ArrayList<>();
        Map<String, String> channelNames = Map.of(
                "0", "초보채널",
                "1", "1채널",
                "2", "2채널",
                "3", "3채널",
                "19", "19세 채널",
                "20", "랭크채널",
                "42", "이벤트채널"
        );

        int allUser = 0;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            for (JsonNode channel : root) {
                String channelId = channel.get("channel_id").asText();
                int userCount = channel.get("user_count").asInt();
                allUser += userCount;

                Map<String, Object> channelInfo = new HashMap<>();
                channelInfo.put("channel_name", channelNames.getOrDefault(channelId, "알 수 없음"));
                channelInfo.put("user_count", userCount);
                channelData.add(channelInfo);
            }

            Map<String, Object> totalInfo = new HashMap<>();
            totalInfo.put("channel_name", "전체 유저");
            totalInfo.put("user_count", allUser);
            channelData.add(totalInfo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "JSON 파싱 실패"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("channels", channelData);

        return ResponseEntity.ok(new ApiResponse<>(true, result, "조회 성공"));
    }



    @Operation(summary = "획초계산 - ✅전체 접근", tags = {"Core"})
    @GetMapping("/records/search")
    public ApiResponse<?> getRecords(@Parameter String nickname) {
        Record record = recordRepository.findByNICKNAMEAndDate(nickname, LocalDate.now());

        if (record == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(true, null , "조회 실패")).getBody();
        }

        UserDataResponse userDataResponse = reportService.getUserData(record.getID());
        UserData userData =  userDataResponse.getUserData();


        int pastWinCount = record.getWin_count();
        int pastLoseCount = record.getLose_count();
        int pastTotalCount = pastWinCount + pastLoseCount;

        int currentWinCount = userData.getWin_count();
        int currentLoseCount = userData.getLose_count();
        int currentTotalCount = currentWinCount + currentLoseCount;

        int todayGames = currentTotalCount - pastTotalCount;

        Map<String, Object> map = new HashMap<>();

        map.put("todaygames", todayGames);
        map.put("current_win_count", currentWinCount);
        map.put("current_lose_count",currentLoseCount);
        map.put("past_win_count", pastWinCount);
        map.put("past_lose_count", pastLoseCount);
        map.put("isTodayLimit", todayGames > 31);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, map , "조회 성공")).getBody();
    }


    @Operation(summary = "검닉 랭킹 - ✅전체 접근", tags = {"Core"})
    @GetMapping("/rank/black")
    public List<?> getBlackRank() {
        return blakRankList();
    }

    public List<?> blakRankList(){
        List<Record> recordList = recordRepository.findAllByDate(LocalDate.now());

        List<Map<String, Object>> result = new ArrayList<>();

        for (Record record : recordList) {
            int color = record.getNickname_color();
            String hex = intToHexColor(color); // "RRGGBB" 형태의 HEX
            int[] rgb = hexToRgb(hex);
            double closeness = calculateBlackCloseness(rgb[0], rgb[1], rgb[2]);
            closeness = Math.round(closeness * 10000.0) / 10000.0;

            Map<String, Object> map = new HashMap<>();
            map.put("nickname", record.getNICKNAME());
            map.put("color", hex);
            map.put("closeness", closeness);
            map.put("isBlack", closeness > 90);

            result.add(map);
        }

        result.sort(
                (o1, o2) -> {
                    double closeness1 = (double) o1.get("closeness");
                    double closeness2 = (double) o2.get("closeness");
                    return Double.compare(closeness2, closeness1);
                }
        );

        return result;
    }

    public static int[] hexToRgb(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.valueOf(hex.substring(0, 2), 16);
        int g = Integer.valueOf(hex.substring(2, 4), 16);
        int b = Integer.valueOf(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    public static double calculateBlackCloseness(int r, int g, int b) {
        double distance = Math.sqrt(r * r + g * g + b * b);
        double closeness = 100.0 * (1.0 - (distance / 441.673));
        return Math.round(closeness * 100.0) / 100.0; // 소수점 둘째 자리까지
    }

    public static String intToHexColor(int color) {
        long unsigned = color & 0xFFFFFFFFL;
        String hex = Long.toHexString(unsigned).toUpperCase();
        if (hex.length() > 6) {
            hex = hex.substring(hex.length() - 6);
        } else {
            hex = String.format("%06X", unsigned);
        }
        return hex;
    }

    @Operation(summary = "길드 랭킹 - ✅전체 접근", tags = {"Core"})
    @GetMapping("/rank/guild")
    public List<?> getGuildBlackRank() {
        return guildBlackRankList();
    }

    public List<?> guildBlackRankList() {
        List<Record> recordList = recordRepository.findAllByDate(LocalDate.now());

        Map<String, Map<String, Object>> guildMap = new HashMap<>();

        for (Record record : recordList) {
            String guildName = record.getGuild_name();
            if (guildName == null || guildName.isEmpty()) continue;

            String key = guildName;

            if (!guildMap.containsKey(key)) {
                int color = record.getGuild_initial_color();
                int bgColor = record.getGuild_initial_background_color();

                String hex = intToHexColor(color);
                int[] rgb = hexToRgb(hex);
                double closeness = calculateBlackCloseness(rgb[0], rgb[1], rgb[2]);
                closeness = Math.round(closeness * 10000.0) / 10000.0;

                String hex2 = intToHexColor(bgColor);
                int[] rgb2 = hexToRgb(hex2);
                double closeness2 = calculateBlackCloseness(rgb2[0], rgb2[1], rgb2[2]);
                closeness2 = Math.round(closeness2 * 10000.0) / 10000.0;

                Map<String, Object> map = new HashMap<>();
                map.put("guild_name", guildName);
                map.put("guild_point", record.getGuild_point());
                map.put("initial_color", hex);
                map.put("initial_background_color", hex2);
                map.put("initial_closeness", closeness);
                map.put("initial_background_closeness", closeness2);
                map.put("guild_initial", record.getGuild_initial());

                guildMap.put(key, map);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(guildMap.values());
        result.sort((o1, o2) -> Double.compare(
                (double) o2.get("initial_background_closeness"),
                (double) o1.get("initial_background_closeness")
        ));

        return result;
    }

}
