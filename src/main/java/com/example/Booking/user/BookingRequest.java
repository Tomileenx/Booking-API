package com.example.Booking.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record BookingRequest(
        @NotBlank(message = "Slot id cannot be empty")
        String slotId,

        @NotBlank(message = "Name cannot be empty")
        String name,

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Please provide a valid email address")
        String email
) {
}
