package com.carenexus.direct.dto;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class MessageDTO {
    private Long id;

    @NotBlank(message = "Sender is required")
    private String sender;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotBlank(message = "Message content cannot be empty")
    private String content;

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    private LocalDateTime timestamp;
}
