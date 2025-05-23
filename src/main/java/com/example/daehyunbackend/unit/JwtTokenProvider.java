package com.example.daehyunbackend.unit;

import com.example.daehyunbackend.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰을 생성하고 검증하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpireTime;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpireTime;

    @Value("${jwt.secret}")
    private String salt;
    private Key secretKey;

    private final JpaUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 액세스 토큰을 생성합니다.
     *
     * @param userId 대상 회원의 아이디
     * @param role 회원의 역할
     * @return 생성된 액세스 토큰
     */
    public String createAccessToken(final Long userId, final Role role) {
        return createToken(userId, role, this.accessTokenExpireTime);
    }

    /**
     * 리프레시 토큰을 생성합니다.
     *
     * @param userId 대상 회원의 아이디
     * @param role 회원의 역할
     * @return 생성된 리프레시 토큰
     */
    public String createRefreshToken(final Long userId, final Role role) {
        return createToken(userId, role, this.refreshTokenExpireTime);
    }

    /**
     * 토큰을 생성합니다.
     *
     * @param userId 대상 회원의 아이디
     * @param role 회원의 역할
     * @param expireTime 만료 시간
     * @return 생성된 토큰
     */
    private String createToken(final Long userId, final Role role, final long expireTime) {
        final Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("roles", role); // TODO roles -> role

        final Date now = new Date();
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + expireTime))
            .signWith(this.secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * JWT 토큰에서 인증 정보를 가져옵니다.
     *
     * @param token JWT 토큰
     * @return 인증 정보
     */
    public Authentication getAuthentication(final String token) {
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(this.getAccount(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * JWT 토큰에서 회원의 계정을 가져옵니다.
     *
     * @param token JWT 토큰
     * @return 회원의 계정
     */
    public String getAccount(final String token) {
        return Jwts.parserBuilder()
            .setSigningKey(this.secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    /**
     * JWT 토큰이 유효한지 검증합니다.
     *
     * @param token JWT 토큰
     * @return 토큰이 유효한 경우 {@code true}를 반환
     */
    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(this.secretKey)
                .build()
                .parseClaimsJws(token);

            return true;
        } catch (final ExpiredJwtException exception) {
            log.warn("토큰이 만료되었습니다. ({})", exception.getClaims().getSubject());
        } catch (final JwtException | IllegalArgumentException exception) {
            log.error("토큰 검증에 실패하였습니다.", exception);
        }
        return false;
    }
}
