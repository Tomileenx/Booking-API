package com.example.Booking.admin;

import java.time.Instant;
import java.util.List;

public record AppointmentSlotResetRequest(
        List<SlotInput> slots
) {
    public record SlotInput(String id, String provider, Instant start, Instant end) {}
}
