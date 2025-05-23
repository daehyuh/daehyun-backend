package com.example.daehyunbackend.service;

import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.repository.AuthRepository;
import com.example.daehyunbackend.repository.UserRepository;
import com.example.daehyunbackend.response.ReportIdResponse;
import com.example.daehyunbackend.response.ReportResponse;
import com.example.daehyunbackend.response.UserDataResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class ReportService {

    public ReportIdResponse getReportId(String nickname, String code) {
        final String url = "https://mafia42.com/php/10th_create_report.php?nickname=" + nickname + "&code=" + code;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5초
        factory.setReadTimeout(5000);

        RestTemplate restTemplate = new RestTemplate(factory);

        System.out.println("before postForObject");
        ReportIdResponse reportIdResponse = restTemplate.postForObject(url, null, ReportIdResponse.class);
        System.out.println("after postForObject");
        return reportIdResponse;
    }

    public ReportResponse getUserId(String reportId) {
        final String url = "https://mafia42.com/php/10th_get_report.php?reportId=" + reportId;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5초
        factory.setReadTimeout(5000);

        RestTemplate restTemplate = new RestTemplate(factory);
        System.out.println("before postForObject");
        ReportResponse reportResponse = restTemplate.postForObject(url, null, ReportResponse.class);
        System.out.println("after postForObject");

        return reportResponse;
    }

    public UserDataResponse getUserData(Long userId) {
        final String url = "https://mafia42.com/api/user/user-info?id=" + userId.toString();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5초
        factory.setReadTimeout(5000);

        RestTemplate restTemplate = new RestTemplate(factory);
        UserDataResponse userDataResponse =  restTemplate.postForObject(url, null, UserDataResponse.class);
        return userDataResponse;
    }



}