package com.example.Booking.user;

import com.example.Booking.exception.TooManyRequests;
import com.example.Booking.idempotency.IdempotencyBookingService;
import com.example.Booking.rateLimit.RateLimitService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final IdempotencyBookingService idempotencyBookingService;
    private final RateLimitService rateLimitService;
    private final Validator validator;

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> bookSlot(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody BookingRequest request
    ) {
        long retryAfter = rateLimitService.consumeAttempt(request.email());
        if (retryAfter > 0) {
            throw new TooManyRequests(retryAfter);
        }

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

       return idempotencyBookingService.book(request, idempotencyKey);
    }

    @GetMapping("/bookings")
    public ResponseEntity<BookingPage> listBookings(
            @RequestParam String email,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(bookingService.listBookings(email, limit, cursor));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable String id
    ) {
        BookingResponse response = bookingService.getBooking(id);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> deleteBooking(
            @PathVariable String id
    ) {
        BookingResponse response = bookingService.deleteBooking(id);

        return ResponseEntity.ok().body(response);
    }
}
