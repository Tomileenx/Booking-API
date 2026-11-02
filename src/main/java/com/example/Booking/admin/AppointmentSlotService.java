package com.example.Booking.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentSlotService {
    private final AppointmentSlotRepo appointmentSlotRepo;

    @Transactional
    public void resetAppointmentSlots() {
        appointmentSlotRepo.deleteAll();

        appointmentSlotRepo.saveAll(generateSlots());
    }

    private List<AppointmentSlot> generateSlots() {
        List<AppointmentSlot> slots = new ArrayList<>();

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

                appointmentSlotRepo.save(AppointmentSlot.builder()
                        .provider("Dr Ade")
                        .start(start)
                        .end(end)
                        .available(true)
                        .build());
            }

            daysCreated++;
        }

        return slots;
    }

    public Page<AppointmentSlotResponse> getAllAppointmentSlots(Pageable pageable) {
        if (pageable.getPageSize() > 8) {
            pageable = PageRequest.of(pageable.getPageNumber(), 8, pageable.getSort());
        }

        Page<AppointmentSlot> pages = appointmentSlotRepo.findAll(pageable);

        return pages.map(
                page -> new AppointmentSlotResponse(
                        page.getId(),
                        page.getProvider(),
                        page.getStart(),
                        page.getEnd(),
                        page.isAvailable()
                )
        );
    }
}
