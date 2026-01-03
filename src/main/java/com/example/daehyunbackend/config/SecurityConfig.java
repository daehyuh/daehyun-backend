package com.example.daehyunbackend.config;

import com.example.daehyunbackend.unit.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import java.util.List;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


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
                .csrf(AbstractHttpConfigurer::disable)        // ✅ CSRF 비활성화
//                .cors(AbstractHttpConfigurer::disable)        // ✅ CORS 비활성화
                .cors(Customizer.withDefaults())
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll() // 반드시 최상단에 위치
                        .requestMatchers("/").permitAll()
                        .requestMatchers(
                                "/core/rank/black",
                                "/core/rank/guild",
                                "/core/records/search",
                                "/core/getChannel",
                                "/core/ad",
                                "/core/ad/**",
                                "/login/oauth2/code/google",
                                "/core/logout",
                                "User/Account/syncGuest",
                                "/Account/syncOneGuest/"
                        ).permitAll()
                        .requestMatchers("/stats/**").permitAll()
                        .requestMatchers("/attach/images/**").permitAll()
                        .requestMatchers(swaggerList).permitAll()
                        .requestMatchers("/User/Account/sync").authenticated()
                        .requestMatchers("/User/Account/syncAll").authenticated()
                        .requestMatchers("/User/Account/syncAllDay").authenticated()
                        .requestMatchers("/User/profile/me").authenticated()
                        .requestMatchers("/Account/addGuest").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "https://xn--vk1b177d.com",
                "http://localhost:5173",
                "https://api.xn--vk1b177d.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
