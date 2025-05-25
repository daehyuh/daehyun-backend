package com.example.daehyunbackend.controller;

import com.example.daehyunbackend.dto.AdCreateRequest;
import com.example.daehyunbackend.entity.Ad;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.RecordRepository;
import com.example.daehyunbackend.response.ApiResponse;
import com.example.daehyunbackend.response.UserData;
import com.example.daehyunbackend.response.UserDataResponse;
import com.example.daehyunbackend.service.AccountService;
import com.example.daehyunbackend.service.AdService;
import com.example.daehyunbackend.service.ReportService;
import com.example.daehyunbackend.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/core")
public class AdController {
    private final AdService adService;

    @Operation(summary = "광고 조회", tags = {"Ad"})
    @GetMapping("/ad")
    public ResponseEntity<?> getAd() {
        try {
            List<Ad> ads = adService.getAllAds();
            return ResponseEntity.ok(new ApiResponse(true, ads , "광고 조회 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "광고 조회 실패", null));
        }
    }

    @Operation(summary = "광고 하나 조회", tags = {"Ad"})
    @GetMapping("/ad/{ID}")
    public ResponseEntity<?> getAd(@PathVariable Long ID) {
        try {
            List<Ad> ads = adService.getAllAds();
            return ResponseEntity.ok(new ApiResponse(true, ads , "광고 조회 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "광고 조회 실패", null));
        }
    }
    
    @Operation(summary = "광고 등록", tags = {"Ad"})
    @PostMapping("/ad")
    public ResponseEntity<?> registerAd(@RequestBody AdCreateRequest ad) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, adService.createAd(ad), "광고 등록 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false,null, "광고 등록 실패"));
        }
    }

    @Operation(summary = "광고 삭제", tags = {"Ad"})
    @DeleteMapping("/ad/{ID}")
    public ResponseEntity<?> deleteAd(@PathVariable Long ID) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, adService.deleteAd(ID), "광고 삭제 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, null, "광고 삭제 실패"));
        }
    }

}
