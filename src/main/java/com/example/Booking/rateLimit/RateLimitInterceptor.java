package com.example.Booking.rateLimit;

import com.example.Booking.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitService rateLimitService;
    private final ObjectMapper mapper;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String email = request.getParameter("email");

        if (email == null || email.isBlank()) {
            return true;
        }

        long retryAfter = rateLimitService.consumeAttempt(email);

        if (retryAfter > 0) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader(
                    HttpHeaders.RETRY_AFTER,
                    String.valueOf(retryAfter)
            );

            ErrorResponse error = new ErrorResponse(
                    "Too many attempts. Please try again later.",
                    "TOO_MANY_REQUESTS",
                    Instant.now()
            );

            mapper.writeValue(response.getWriter(), error);

            return false;
        }

        return true;
    }
}
