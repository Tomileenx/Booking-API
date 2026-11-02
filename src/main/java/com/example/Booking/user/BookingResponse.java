package com.example.Booking.user;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        String id,
        String slotId,
        String name,
        String email,
        BookingStatus status,
        Instant createdAt
) {
}
