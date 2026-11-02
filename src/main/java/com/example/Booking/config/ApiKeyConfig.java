package com.example.Booking.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ApiKeyConfig {
    @Value("${clinic.api.key}")
    private String apiKey;
}
