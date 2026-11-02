package com.example.Booking.exception;

import lombok.Getter;

@Getter
public class TooManyRequests extends RuntimeException {
    private final long retryAfterSeconds;

    public TooManyRequests(long retryAfterSeconds) {
        super("Too many attempts. Please try again later.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

}
