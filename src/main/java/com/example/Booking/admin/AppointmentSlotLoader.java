package com.example.Booking.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
public class AppointmentSlotLoader {

    @Bean
    CommandLineRunner loadAppointments(AppointmentSlotRepo repo, AppointmentSlotFactory factory) {
        return args -> {
            if (repo.count() == 0) {
                repo.saveAll(factory.generateMonth());
            }
        };
    }
}