package com.carenexus.direct.controller;

import com.carenexus.direct.dto.AppointmentDTO;
import com.carenexus.direct.mapper.AppointmentMapper;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.model.Patient;
import com.carenexus.direct.service.AppointmentService;
import com.carenexus.direct.service.DoctorService;
import com.carenexus.direct.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor

public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentDTO dto) {
        Doctor doctor = doctorService.getById(dto.getDoctorId());
        Patient patient = patientService.getById(dto.getPatientId());
        Appointment appointment = AppointmentMapper.toEntity(dto, doctor, patient);
        Appointment saved = appointmentService.save(appointment);
        return ResponseEntity.ok(AppointmentMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getAll()
                .stream()
                .map(AppointmentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        return ResponseEntity.ok(AppointmentMapper.toDto(appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
