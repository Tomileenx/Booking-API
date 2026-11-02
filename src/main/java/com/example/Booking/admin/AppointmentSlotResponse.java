package com.example.Booking.admin;

import java.time.Instant;

public record AppointmentSlotResponse(
        String id,
        String provider,
        Instant start,
        Instant end,
        boolean available
) {
}
