package com.example.Booking.admin;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @ApiResponse(
            responseCode = "200",
            description = "Appointment slots reset successfully",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Done")
            )
    )
    @PostMapping("/reset")
    public ResponseEntity<String> resetAppointmentSlots(
            @Valid @RequestBody AppointmentSlotResetRequest request
    ) {
        appointmentSlotService.resetAppointmentSlots(request);
        return ResponseEntity.ok("Done");
    }

    @ApiResponse(
            responseCode = "200",
            description = "Appointment slots retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = AppointmentSlotResponse.class)
                    )
            )
    )
    @GetMapping("/slots")
    public ResponseEntity<List<AppointmentSlotResponse>> getSlots(
            @RequestParam(required = false) String provider
    ) {
        List<AppointmentSlotResponse> appointmentSlotResponses =
                appointmentSlotService.getAllAppointmentSlots(provider);

        return ResponseEntity.ok(appointmentSlotResponses);
    }
}
