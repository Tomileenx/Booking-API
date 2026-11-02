package com.example.Booking.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "idempotency_record")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class IdempotencyRecord {
    @Id
    @Column(name = "idempotency_key")
    private String key;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false)
    private int responseStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    private Instant createdAt;
}
