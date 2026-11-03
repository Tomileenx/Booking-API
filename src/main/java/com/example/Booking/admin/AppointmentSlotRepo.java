package com.example.Booking.admin;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AppointmentSlotRepo extends JpaRepository<AppointmentSlot, String> {

    List<AppointmentSlot> findByProvider(String provider, Sort sort);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update AppointmentSlot s set s.available = false " +
            "where s.id = :id and s.available = true and s.start > :now")
    int claimSlot(@Param("id") String id, @Param("now") Instant now);
}
