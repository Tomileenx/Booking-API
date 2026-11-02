package com.example.Booking.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
    private final ApiKeyConfig apiKeyConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String suppliedKey = request.getHeader("X-API-Key");
        String configuredKey = apiKeyConfig.getApiKey();

        if (suppliedKey == null || configuredKey == null ||
                !MessageDigest.isEqual(
                        suppliedKey.getBytes(StandardCharsets.UTF_8),
                        configuredKey.getBytes(StandardCharsets.UTF_8)
                )) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    "{\"error\":\"Missing or invalid API key\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}
