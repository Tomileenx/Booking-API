package com.example.Booking.admin;

import com.example.Booking.exception.BadRequest;
import com.example.Booking.idempotency.IdempotencyRecordRepo;
import com.example.Booking.user.BookingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentSlotService {
    private final AppointmentSlotRepo appointmentSlotRepo;
    private final BookingRepo bookingRepo;
    private final IdempotencyRecordRepo idempotencyRecordRepo;
    private final AppointmentSlotFactory slotFactory;

    @Transactional
    public void resetAppointmentSlots(AppointmentSlotResetRequest request) {
       if (request == null || request.slots() == null) {
           throw new BadRequest("slots is required");
       }

        List<AppointmentSlot> slots = new ArrayList<>();
        for (AppointmentSlotResetRequest.SlotInput s : request.slots()) {
            if (s == null
                    || s.provider() == null || s.provider().isBlank()
                    || s.start() == null || s.end() == null
                    || !s.end().isAfter(s.start())) {
                throw new BadRequest("Each slot needs provider, start, and an end after start");
            }
            slots.add(slotFactory.create(s.provider().trim(), s.start(), s.end()));
        }

        idempotencyRecordRepo.deleteAllInBatch();
        bookingRepo.deleteAllInBatch();
        appointmentSlotRepo.deleteAllInBatch();
        appointmentSlotRepo.saveAll(slots);
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> getAllAppointmentSlots(String provider) {
        Sort sort = Sort.by(Sort.Direction.ASC, "start", "id");

       List<AppointmentSlot> slots =
               (provider == null || provider.isBlank())
               ? appointmentSlotRepo.findAll(sort)
                       : appointmentSlotRepo.findByProvider(provider, sort);

       return slots.stream()
            .map(s-> new AppointmentSlotResponse(
                    s.getId(),
                    s.getProvider(),
                    s.getStart(),
                    s.getEnd(),
                    s.isAvailable())
            ).toList();
    }
}
