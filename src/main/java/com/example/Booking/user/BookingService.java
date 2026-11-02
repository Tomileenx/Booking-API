package com.example.Booking.user;

import com.example.Booking.admin.AppointmentSlot;
import com.example.Booking.admin.AppointmentSlotRepo;
import com.example.Booking.exception.AlreadyExists;
import com.example.Booking.exception.BadRequest;
import com.example.Booking.exception.NotFound;
import com.example.Booking.idempotency.IdempotencyRecord;
import com.example.Booking.idempotency.IdempotencyRecordRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepo bookingRepo;
    private final AppointmentSlotRepo slotRepo;
    private final IdempotencyRecordRepo idempotencyRecordRepo;
    private final ObjectMapper mapper;

    @Transactional
    public BookingResponse bookSlotWithKey(BookingRequest request, String key, String hash) {
        BookingResponse response = bookSlot(request);

        try {
            idempotencyRecordRepo.saveAndFlush(
                    IdempotencyRecord.builder()
                            .key(key)
                            .requestHash(hash)
                            .responseStatus(201)
                            .responseBody(mapper.writeValueAsString(response))
                            .createdAt(Instant.now())
                            .build()
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

        return response;
    }

    @Transactional
    public BookingResponse bookSlot(BookingRequest request) {
        if (request.slotId() == null) {
            throw new BadRequest("Name cannot be empty");
        }

        if (request.name() == null) {
            throw new BadRequest("Name cannot be empty");
        }

        if (slotRepo.claimSlot(request.slotId()) == 0) {
            if (!slotRepo.existsById(request.slotId())) {
                throw new NotFound("Slot Id not found");
            }
            throw new AlreadyExists("This slot is already booked");   // -> 409
        }

        AppointmentSlot slot = slotRepo.getReferenceById(request.slotId());

        Booking booking = Booking.builder()
                .slot(slot)
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .status(BookingStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build();

        slot.setAvailable(false);

        try {
            booking = bookingRepo.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExists("This slot is already booked");
        }

        return new BookingResponse(
                booking.getId(),
                slot.getId(),
                booking.getName(),
                booking.getEmail(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public BookingPage listBookings(String email, int limit, String cursor) {
        if (limit < 1 || limit > 50) {
            throw new BadRequest("limit must be between 1 and 50");
        }

        Pageable page = PageRequest.of(0, limit + 1);   // one extra to detect more
        List<Booking> rows;

        if (cursor == null || cursor.isBlank()) {
            rows = bookingRepo.findByEmailIgnoreCaseOrderByCreatedAtAscIdAsc(email, page);
        } else {
            Instant createdAt;
            String id;
            try {
                String[] parts = new String(Base64.getUrlDecoder().decode(cursor),
                        StandardCharsets.UTF_8).split("\\|", 2);
                createdAt = Instant.parse(parts[0]);
                id = parts[1];
            } catch (Exception e) {
                throw new BadRequest("Invalid cursor");
            }
            rows = bookingRepo.findNextPage(email, createdAt, id, page);
        }

        String nextCursor = null;
        if (rows.size() > limit) {
            rows = rows.subList(0, limit);
            Booking last = rows.get(limit - 1);
            nextCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    (last.getCreatedAt() + "|" + last.getId()).getBytes(StandardCharsets.UTF_8));
        }

        List<BookingResponse> items = rows.stream()
                .map(b -> new BookingResponse(
                        b.getId(), b.getSlot().getId(), b.getName(),
                        b.getEmail(), b.getStatus(), b.getCreatedAt()))
                .toList();

        return new BookingPage(items, nextCursor);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(String id) {
        Booking book = bookingRepo.findById(id)
                .orElseThrow(() -> new NotFound("Booking not found"));

        return new BookingResponse(
                book.getId(),
                book.getSlot().getId(),
                book.getName(),
                book.getEmail(),
                book.getStatus(),
                book.getCreatedAt()
        );
    }

    @Transactional
    public BookingResponse deleteBooking(String id) {
        Booking book = bookingRepo.findById(id)
                .orElseThrow(() -> new NotFound("Booking not found"));

        if (book.getStatus() == BookingStatus.CANCELLED) {
            throw new AlreadyExists("This booking is already cancelled");
        }

        book.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(book);

        return new BookingResponse(
                book.getId(),
                book.getSlot().getId(),
                book.getName(),
                book.getEmail(),
                book.getStatus(),
                book.getCreatedAt()
        );
    }
}
