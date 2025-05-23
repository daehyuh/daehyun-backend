package com.example.daehyunbackend.service;

import com.example.daehyunbackend.controller.AdController;
import com.example.daehyunbackend.dto.AdCreateRequest;
import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Ad;
import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdService {
    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private final AdRepository Adrepository;

    public List<Ad> getAllAds() {
        return Adrepository.findAll();
    }

    public Long deleteAd(Long adId) {
        Ad ad = Adrepository.findById(adId).orElseThrow(() -> new RuntimeException("Ad not found"));
        Adrepository.delete(ad);
        return adId;
    }

    public Ad getAdById(Long adId) {
        return Adrepository.findById(adId).orElseThrow(() -> new RuntimeException("Ad not found"));
    }

    public Ad createAd(AdCreateRequest adr) throws IOException {
        String fileName = adr.getImage().getOriginalFilename();

        Path filePath = Paths.get(imageUploadDir, fileName);

        Files.createDirectories(filePath.getParent());

        Files.write(filePath, adr.getImage().getBytes());

        Ad ad = Ad.builder()
                .url(fileName)
                .startDate(adr.getStartDate())
                .endDate(adr.getEndDate())
                .href(adr.getHref())
                .build();

        return Adrepository.save(ad);
    }

    public Ad updateAd(Long adId, Ad ad) {
        Ad existingAd = Adrepository.findById(adId).orElseThrow(() -> new RuntimeException("Ad not found"));
        existingAd.setUrl(ad.getUrl());
        existingAd.setStartDate(ad.getStartDate());
        existingAd.setEndDate(ad.getEndDate());
        existingAd.setHref(ad.getHref());
        return Adrepository.save(existingAd);
    }


}
