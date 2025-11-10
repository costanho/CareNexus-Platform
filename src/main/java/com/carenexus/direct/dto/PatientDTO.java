package com.carenexus.direct.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDTO {
    private Long id;

    @NotBlank(message = "Patient name is required")
    private String name;

    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    private String phone;
}
