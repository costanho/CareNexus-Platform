package com.carenexus.direct.controller;

import com.carenexus.direct.dto.PatientDTO;
import com.carenexus.direct.mapper.PatientMapper;
import com.carenexus.direct.model.Patient;
import com.carenexus.direct.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO dto) {
        Patient patient = PatientMapper.toEntity(dto);
        Patient saved = patientService.save(patient);
        return ResponseEntity.ok(PatientMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAll()
                .stream()
                .map(PatientMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getById(id);
        return ResponseEntity.ok(PatientMapper.toDto(patient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
