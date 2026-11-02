package com.example.Booking.admin;

import com.example.Booking.exception.BadRequest;
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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentSlotService {
    private final AppointmentSlotRepo appointmentSlotRepo;

    private static final int MAX_PAGE_SIZE = 40;


    @Transactional
    public void resetAppointmentSlots() {
        appointmentSlotRepo.deleteAll();

        appointmentSlotRepo.saveAll(generateSlots());
    }

    private List<AppointmentSlot> generateSlots() {
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

    public Page<AppointmentSlotResponse> getAllAppointmentSlots(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BadRequest("size must be " + MAX_PAGE_SIZE + " or less");
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
