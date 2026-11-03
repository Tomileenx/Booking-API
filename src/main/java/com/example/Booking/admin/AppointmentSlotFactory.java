package com.example.Booking.admin;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
public class AppointmentSlotFactory {
    public AppointmentSlot create(String provider, Instant start, Instant end) {
        return AppointmentSlot.builder()
                .provider(provider)
                .start(start)
                .end(end)
                .available(start.isAfter(Instant.now()))
                .build();
    }

    public List<AppointmentSlot> generateMonth() {
        List<AppointmentSlot> slots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate last = today.with(TemporalAdjusters.lastDayOfMonth());

        for (LocalDate d = today.withDayOfMonth(1); !d.isAfter(last); d = d.plusDays(1)) {
            if (d.getDayOfWeek() == DayOfWeek.SATURDAY
                    || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            for (int hour = 9; hour <= 16; hour++) {
                Instant start = d.atTime(hour, 0).toInstant(ZoneOffset.UTC);
                slots.add(create("Dr Ade", start, start.plus(30, ChronoUnit.MINUTES)));
            }
        }
        return slots;
    }
}
