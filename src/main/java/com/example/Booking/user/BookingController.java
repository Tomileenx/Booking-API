package com.example.Booking.user;

import com.example.Booking.exception.TooManyRequests;
import com.example.Booking.idempotency.IdempotencyBookingService;
import com.example.Booking.rateLimit.RateLimitService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Booking created or existing idempotent booking returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/400"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/404"),
            @ApiResponse(responseCode = "422", ref = "#/components/responses/422"),
            @ApiResponse(responseCode = "429", ref = "#/components/responses/429"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/500")
    })
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

    @ApiResponse(
            responseCode = "200",
            description = "Bookings retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingResponse.class)
            )
    )
    @GetMapping("/bookings")
    public ResponseEntity<BookingPage> listBookings(
            @RequestParam String email,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(bookingService.listBookings(email, limit, cursor));
    }

    @ApiResponse(
            responseCode = "200",
            description = "Booking retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingResponse.class)
            )
    )
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable String id
    ) {
        BookingResponse response = bookingService.getBooking(id);

        return ResponseEntity.ok().body(response);
    }

    @ApiResponse(
            responseCode = "200",
            description = "Booking cancelled successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingResponse.class)
            )
    )
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> deleteBooking(
            @PathVariable String id
    ) {
        BookingResponse response = bookingService.deleteBooking(id);

        return ResponseEntity.ok().body(response);
    }
}
