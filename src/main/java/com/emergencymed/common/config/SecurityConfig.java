package com.emergencymed.common.config;

import com.emergencymed.common.api.ApiResponse;
import com.emergencymed.common.api.ErrorItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.PrintWriter;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register/**",
                    "/api/v1/payments/callback",
                    "/api/v1/chatbot/message",
                    "/h2-console/**"
                )
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                // Static web pages and assets (public)
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/register-*.html",
                    "/profile.html",
                    "/search.html",
                    "/medicine-catalogue.html",
                    "/notifications.html",
                    "/checkout.html",
                    "/payment-status.html",
                    "/chat.html",
                    "/dashboard/**",
                    "/admin/**",
                    "/pharmacy/**",
                    "/delivery/**",
                    "/request/**",
                    "/assets/**",
                    "/favicon.ico",
                    "/h2-console/**"
                ).permitAll()

                // Public REST APIs
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register/**",
                    "/api/v1/auth/csrf",
                    "/api/v1/payments/callback",
                    "/api/v1/chatbot/message"
                ).permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v1/search/**", "/api/v1/medicines/**", "/api/v1/pharmacies/**", "/api/v1/categories/**", "/api/v1/inventory/**", "/api/v1/pharmacy-settings/**").permitAll()

                // Role-restricted APIs
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/pharmacies/*/approve", "/api/v1/pharmacies/*/reject").hasRole("ADMIN")
                .requestMatchers("/api/v1/pharmacy/**").hasAnyRole("PHARMACY", "ADMIN")

                // All other API endpoints require authentication
                .requestMatchers("/api/v1/**").authenticated()

                .anyRequest().permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<Void> apiResp = ApiResponse.error(
                        "Authentication required. Please log in.",
                        List.of(ErrorItem.of("UNAUTHORIZED", authException.getMessage()))
                    );
                    PrintWriter writer = response.getWriter();
                    writer.write(objectMapper.writeValueAsString(apiResp));
                    writer.flush();
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<Void> apiResp = ApiResponse.error(
                        "Access forbidden: Insufficient permissions for this action.",
                        List.of(ErrorItem.of("FORBIDDEN", accessDeniedException.getMessage()))
                    );
                    PrintWriter writer = response.getWriter();
                    writer.write(objectMapper.writeValueAsString(apiResp));
                    writer.flush();
                })
            );

        return http.build();
    }
}
