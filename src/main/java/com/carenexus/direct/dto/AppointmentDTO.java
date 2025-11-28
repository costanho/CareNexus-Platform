package com.carenexus.direct.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {

    private Long id;

    private Long doctorId;
    private Long patientId;

    private LocalDateTime appointmentTime;
    private String reason;
}
