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
    CommandLineRunner loadAppointments(AppointmentSlotRepo repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.saveAll(generateSlots());
        };
    }

    static List<AppointmentSlot> generateSlots() {
        List<AppointmentSlot> slots = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = today.with(TemporalAdjusters.lastDayOfMonth());

        for (LocalDate date = first; !date.isAfter(last); date = date.plusDays(1)) {

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            for (int hour = 9; hour <= 16; hour++) {
                Instant start = date.atTime(hour, 0).toInstant(ZoneOffset.UTC);
                Instant end = start.plus(30, ChronoUnit.MINUTES);

                slots.add(AppointmentSlot.builder()
                        .provider("Dr Ade")
                        .start(start)
                        .end(end)
                        .available(true)
                        .build());
            }
        }
        return slots;
    }
}