package com.example.Booking.exception;

public class UnprocessableContent extends RuntimeException {
    public UnprocessableContent(String message) {
        super(message);
    }
}
