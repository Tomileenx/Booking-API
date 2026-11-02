package com.example.Booking.idempotency;

import com.example.Booking.exception.AlreadyExists;
import com.example.Booking.exception.UnprocessableContent;
import com.example.Booking.user.BookingRequest;
import com.example.Booking.user.BookingResponse;
import com.example.Booking.user.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyBookingService {
    private final BookingService bookingService;
    private final IdempotencyRecordRepo idempotencyRecordRepo;
    private final ObjectMapper mapper;

    public ResponseEntity<BookingResponse> book(BookingRequest request, String key) {
        if (key == null || key.isBlank()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(bookingService.bookSlot(request));
        }

        String hash = hash(request);

        Optional<IdempotencyRecord> existing = idempotencyRecordRepo.findById(key);
        if (existing.isPresent()) {
            return replay(existing.get(), hash);
        }

        try {
            BookingResponse response = bookingService.bookSlotWithKey(request, key, hash);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException | AlreadyExists e) {
            Optional<IdempotencyRecord> rec = idempotencyRecordRepo.findById(key);
            if (rec.isPresent()) {
                return replay(rec.get(), hash);
            }
            throw e;
        }
    }

    private ResponseEntity<BookingResponse> replay(IdempotencyRecord rec, String hash) {
        if (!rec.getRequestHash().equals(hash)) {
            throw new UnprocessableContent("Idempotency-Key was already used with a different request body");
        }
        try {
            return ResponseEntity.status(rec.getResponseStatus())
                    .body(mapper.readValue(rec.getResponseBody(), BookingResponse.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String hash(BookingRequest r) {
        try {
            String normalized = r.slotId() + "|"
                    + r.name().trim() + "|"
                    + r.email().trim().toLowerCase();
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}