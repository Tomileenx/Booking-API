package com.example.Booking.exception;

public class SessionExpired extends RuntimeException {
    public SessionExpired(String message) {
        super(message);
    }
}
