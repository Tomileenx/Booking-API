package com.example.Booking.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BookingPage(
        List<BookingResponse> items,
        @JsonProperty("next_cursor") String nextCursor
) {
}
