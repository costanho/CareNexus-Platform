package com.carenexus.direct.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    private Long id;
    private Long appointmentId;

    @NotBlank(message = "Sender is required")
    private String sender;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotBlank(message = "Message cannot be empty")
    private String content;
}
