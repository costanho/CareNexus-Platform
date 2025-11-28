package com.carenexus.direct.controller;

import com.carenexus.direct.dto.AppointmentDTO;
import com.carenexus.direct.mapper.AppointmentMapper;
import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.model.Patient;
import com.carenexus.direct.service.AppointmentService;
import com.carenexus.direct.service.DoctorService;
import com.carenexus.direct.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
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
    public ResponseEntity<AppointmentDTO> createAppointment(
            @RequestBody AppointmentDTO dto,
            Principal principal
    ) {
        String email = principal.getName();

        Doctor doctor = doctorService.getById(dto.getDoctorId());
        Patient patient = patientService.getById(dto.getPatientId());

        var appointment = AppointmentMapper.toEntity(dto, doctor, patient, email);
        var saved = appointmentService.save(appointment);

        return ResponseEntity.ok(AppointmentMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(Principal principal) {
        String email = principal.getName();

        List<AppointmentDTO> list = appointmentService.getAllByUser(email)
                .stream()
                .map(AppointmentMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(
            @PathVariable Long id,
            Principal principal
    ) {
        String email = principal.getName();
        var appointment = appointmentService.getByIdAndUser(id, email);
        return ResponseEntity.ok(AppointmentMapper.toDto(appointment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentDTO dto,
            Principal principal
    ) {
        String email = principal.getName();
        var updated = appointmentService.update(id, AppointmentMapper.toEntity(dto, null, null, email), email);
        return ResponseEntity.ok(AppointmentMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable Long id,
            Principal principal
    ) {
        appointmentService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<Page<AppointmentDTO>> getAppointmentsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appointmentTime") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            Principal principal) {

        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AppointmentDTO> result = appointmentService.getAllWithPagination(email, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-reason")
    public ResponseEntity<Page<AppointmentDTO>> searchAppointmentsByReason(
            @RequestParam String reason,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reason") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            Principal principal) {

        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AppointmentDTO> result = appointmentService.searchByReason(email, reason, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-date-range")
    public ResponseEntity<Page<AppointmentDTO>> searchAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appointmentTime") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            Principal principal) {

        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AppointmentDTO> result = appointmentService.searchByDateRange(email, startDate, endDate, pageable);

        return ResponseEntity.ok(result);
    }
}
