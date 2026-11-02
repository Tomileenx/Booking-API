package com.example.Booking.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Configuration
public class AppointmentSlotLoader {
    @Bean
    CommandLineRunner loadAppointments(AppointmentSlotRepo repo) {

        return args -> {

            if (repo.count() > 0) {
                return;
            }

            LocalDate date = LocalDate.now();
            int daysCreated = 0;

            while (daysCreated < 5) {
                date = date.plusDays(1);

                if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                        || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    continue;
                }

                for (int hour = 9; hour <= 16; hour++) {
                    Instant start = date
                            .atTime(hour, 0)
                            .toInstant(ZoneOffset.UTC);

                    Instant end = start.plus(30, ChronoUnit.MINUTES);

                    repo.save(AppointmentSlot.builder()
                            .provider("Dr Ade")
                            .start(start)
                            .end(end)
                            .available(true)
                            .build());
                }

                daysCreated++;
            }
        };
    }
}
