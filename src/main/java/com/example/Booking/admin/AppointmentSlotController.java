package com.example.Booking.admin;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AppointmentSlotController {
    private final AppointmentSlotService appointmentSlotService;

    @PostMapping("/reset")
    public ResponseEntity<String> resetAppointmentSlots(
    ) {
        appointmentSlotService.resetAppointmentSlots();

        return ResponseEntity.ok("Done");
    }

    @GetMapping("/slots")
    public ResponseEntity<Page<AppointmentSlotResponse>> getSlots(
            @ParameterObject
            @PageableDefault(
                    page = 0,
                    size = 8,
                    sort = "start",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        Page<AppointmentSlotResponse> appointmentSlotResponses =
                appointmentSlotService.getAllAppointmentSlots(pageable);

        return ResponseEntity.ok(appointmentSlotResponses);
    }
}
