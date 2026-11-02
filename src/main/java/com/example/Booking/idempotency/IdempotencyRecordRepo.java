package com.example.Booking.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepo extends JpaRepository<IdempotencyRecord, String> {
}
