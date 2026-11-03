package com.example.Booking.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppointmentSlotController {
    private final AppointmentSlotService appointmentSlotService;

    @PostMapping("/reset")
    public ResponseEntity<String> resetAppointmentSlots(
            @Valid @RequestBody AppointmentSlotResetRequest request
    ) {
        appointmentSlotService.resetAppointmentSlots(request);
        return ResponseEntity.ok("Done");
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AppointmentSlotResponse>> getSlots(
            @RequestParam(required = false) String provider
    ) {
        List<AppointmentSlotResponse> appointmentSlotResponses =
                appointmentSlotService.getAllAppointmentSlots(provider);

        return ResponseEntity.ok(appointmentSlotResponses);
    }
}
