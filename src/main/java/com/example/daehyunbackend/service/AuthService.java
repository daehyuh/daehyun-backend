package com.example.daehyunbackend.service;

import com.example.daehyunbackend.dto.AuthResponseDTO;
import com.example.daehyunbackend.entity.Auth;
import com.example.daehyunbackend.entity.MobileAuthTicket;
import com.example.daehyunbackend.entity.MobileOAuthState;
import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.AuthRepository;
import com.example.daehyunbackend.repository.MobileAuthTicketRepository;
import com.example.daehyunbackend.repository.MobileOAuthStateRepository;
import com.example.daehyunbackend.repository.UserRepository;
import com.example.daehyunbackend.unit.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRepository authRepository;
    private final MobileOAuthStateRepository mobileOAuthStateRepository;
    private final MobileAuthTicketRepository mobileAuthTicketRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    String redirectUri;
    @Value("${google.oauth.mobile-redirect-uri}")
    String mobileRedirectUri;
    @Value("${mobile.oauth.state-ttl-seconds:600}")
    long mobileOAuthStateTtlSeconds;
    @Value("${mobile.oauth.ticket-ttl-seconds:120}")
    long mobileOAuthTicketTtlSeconds;

    public AuthResponseDTO socialLogin(String code) {
        return AuthResponseDTO.fromEntity(authenticateGoogle(code, redirectUri));
    }

    public String createMobileAuthorizationUrl() {
        String rawState = createOpaqueToken();
        LocalDateTime now = LocalDateTime.now();
        mobileOAuthStateRepository.save(new MobileOAuthState(
                hash(rawState),
                now.plusSeconds(mobileOAuthStateTtlSeconds),
                now
        ));

        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", mobileRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", rawState)
                .build()
                .toUriString();
    }

    @Transactional
    public boolean isMobileOAuthState(String rawState) {
        if (rawState == null || rawState.isBlank()) {
            return false;
        }

        return mobileOAuthStateRepository.findByStateHash(hash(rawState))
                .filter(state -> state.isUsable(LocalDateTime.now()))
                .isPresent();
    }

    @org.springframework.transaction.annotation.Transactional
    public String createMobileLoginTicket(String code, String rawState) {
        LocalDateTime now = LocalDateTime.now();
        MobileOAuthState state = mobileOAuthStateRepository.findByStateHash(hash(rawState))
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 OAuth 상태입니다."));
        state.consume(now);

        Auth auth = authenticateGoogle(code, mobileRedirectUri);
        String rawTicket = createOpaqueToken();
        mobileAuthTicketRepository.save(new MobileAuthTicket(
                hash(rawTicket),
                auth.getUser(),
                now.plusSeconds(mobileOAuthTicketTtlSeconds),
                now
        ));
        return rawTicket;
    }

    @org.springframework.transaction.annotation.Transactional
    public AuthResponseDTO exchangeMobileTicket(String rawTicket) {
        LocalDateTime now = LocalDateTime.now();
        MobileAuthTicket ticket = mobileAuthTicketRepository.findByTicketHash(hash(rawTicket))
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 로그인 티켓입니다."));
        ticket.consume(now);

        return authRepository.findByUser(ticket.getUser())
                .map(AuthResponseDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보를 찾을 수 없습니다."));
    }

    @org.springframework.transaction.annotation.Transactional
    public AuthResponseDTO refreshMobileToken(String refreshToken) {
        Auth auth = authRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."));
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다.");
        }

        User user = auth.getUser();
        auth.updateAccessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getRole()));
        auth.updateRefreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getRole()));
        return AuthResponseDTO.fromEntity(authRepository.save(auth));
    }

    private Auth authenticateGoogle(String code, String callbackRedirectUri) {
        String accessToken = getAccessToken(code, callbackRedirectUri);
        JsonNode userResourceNode = getUserResource(accessToken);
        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String name = userResourceNode.get("name").asText();
        String picture = userResourceNode.get("picture").asText();

        Optional<User> userEntity = userRepository.findByProviderId(id);
        User user;
        Auth auth;

        if (userEntity.isEmpty()) {
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
            auth = authRepository.findByUser(user).orElseGet(() -> Auth.builder().user(user).build());
            auth.updateAccessToken(this.jwtTokenProvider.createAccessToken(user.getId(), user.getRole()));
            auth.updateRefreshToken(this.jwtTokenProvider.createRefreshToken(user.getId(), user.getRole()));

            // 변경가능한 정보
            user.setName(name);
            user.setAvatarUrl(picture);

            userRepository.save(user);
            auth = authRepository.save(auth);
        }

        return auth;
    }

    private String getAccessToken(String authorizationCode, String callbackRedirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", callbackRedirectUri);
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

    private String createOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }


}
