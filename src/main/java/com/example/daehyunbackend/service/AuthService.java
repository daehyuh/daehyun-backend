package com.example.daehyunbackend.service;

import com.example.daehyunbackend.dto.AuthResponseDTO;
import com.example.daehyunbackend.entity.Auth;
import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.AuthRepository;
import com.example.daehyunbackend.repository.UserRepository;
import com.example.daehyunbackend.unit.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRepository authRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    String redirectUri;

    public AuthResponseDTO socialLogin(String code) {
        String accessToken = getAccessToken(code);
        JsonNode userResourceNode = getUserResource(accessToken);
        System.out.println("userResourceNode = " + userResourceNode);
        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();
        String picture = userResourceNode.get("picture").asText();

        Optional<User> userEntity = userRepository.findByProviderId(id);
        User user;
        Auth auth;

        System.out.println("userEntity = " + userEntity);

        if (userEntity.isEmpty()) {
            System.out.println(email + "회원가입");
            user = userRepository.save(User.builder()
                    .providerId(id)
                    .provider("google")
                    .email(email)
                    .name(name)
                    .role(Role.ROLE_USER)
                    .avatarUrl(picture)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            auth = authRepository.save(Auth.builder()
                    .user(user)
                    .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getRole()))
                    .refreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getRole()))
                    .build());
        } else {
            user = userEntity.get();
            System.out.println(email + "로그인");

            auth = authRepository.existsByUser(user) ? authRepository.findByUser(user).get() : Auth.builder().user(user).build();
            auth.updateAccessToken(this.jwtTokenProvider.createAccessToken(user.getId(), user.getRole()));
            auth.updateRefreshToken(this.jwtTokenProvider.createRefreshToken(user.getId(), user.getRole()));

            // 변경가능한 정보
            user.setName(name);
            user.setAvatarUrl(picture);

            userRepository.save(user);
        }
        AuthResponseDTO authResponseDTO =  AuthResponseDTO.fromEntity(auth);
        return authResponseDTO;

    }

    private String getAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity entity = new HttpEntity(params, headers);

        ResponseEntity<JsonNode> responseNode = restTemplate.exchange("https://oauth2.googleapis.com/token", HttpMethod.POST, entity, JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        return accessTokenNode.get("access_token").asText();
    }


    private JsonNode getUserResource(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange("https://www.googleapis.com/oauth2/v2/userinfo", HttpMethod.GET, entity, JsonNode.class).getBody();
    }


}