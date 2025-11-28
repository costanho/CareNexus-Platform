package com.carenexus.direct.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String recipient;

    @Column(nullable = false)
    private String content;

    private LocalDateTime timestamp;

    @ManyToOne
    private Appointment appointment;

    /** 🔥 Ownership — who created this message */
    @Column(nullable = false)
    private String userEmail;
}
