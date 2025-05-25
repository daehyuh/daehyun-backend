package com.example.daehyunbackend.config;

import com.example.daehyunbackend.unit.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${admin.password}")
    private String adminPassword;


    private static final String[] swaggerList = {"/swagger-ui/**",
            "/h2-console/**",
            "/favicon.ico",
            "/error",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // CORS Preflight (Option) 요청 허용
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        // 타임리프 템플릿 허용
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/core/ad").permitAll()
                        .requestMatchers(
                                "/core/rank/black",
                                "/core/rank/guild",
                                "/core/records/search",
                                "/core/ad",
                                "/core/ad/**",
                                "/login/oauth2/code/google",
                                "/core/logout"
                        ).permitAll()
                        .requestMatchers(swaggerList).permitAll()
                        .requestMatchers("/User/Account/sync").authenticated()
                        .requestMatchers("/User/profile/me").authenticated()
                        // 기타 모든 요청은 인증 필요
//                        .anyRequest().authenticated()
                )
                .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}