package com.carenexus.direct.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime appointmentTime;
    private String reason;

    @ManyToOne
    private Doctor doctor;




    @ManyToOne
    private Patient patient;

    /** 🔥 ADD THIS */
    @Column(nullable = false)
    private String userEmail;
}
