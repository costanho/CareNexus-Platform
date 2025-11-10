package com.carenexus.direct.controller;

import com.carenexus.direct.dto.DoctorDTO;
import com.carenexus.direct.mapper.DoctorMapper;
import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @RequestBody DoctorDTO dto) {
        Doctor doctor = DoctorMapper.toEntity(dto);
        Doctor saved = doctorService.save(doctor);
        return ResponseEntity.ok(DoctorMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<DoctorDTO> doctors = doctorService.getAll()
                .stream()
                .map(DoctorMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(doctors);
    }
}
