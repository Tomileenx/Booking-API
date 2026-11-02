package com.example.Booking.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingRepo extends JpaRepository<Booking, String> {
    List<Booking> findByEmailIgnoreCaseOrderByCreatedAtAscIdAsc(String email, Pageable pageable);

    // following pages
    @Query("""
        select b from Booking b
        where lower(b.email) = lower(:email)
          and (b.createdAt > :createdAt
               or (b.createdAt = :createdAt and b.id > :id))
        order by b.createdAt asc, b.id asc
        """)
    List<Booking> findNextPage(@Param("email") String email,
                               @Param("createdAt") Instant createdAt,
                               @Param("id") String id,
                               Pageable pageable);
}
