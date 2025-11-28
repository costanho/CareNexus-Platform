package com.carenexus.direct.controller;

import com.carenexus.direct.dto.DoctorDTO;
import com.carenexus.direct.mapper.DoctorMapper;
import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /** ⭐ CREATE DOCTOR PROFILE (doctor’s own profile) */
    @PostMapping
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @RequestBody DoctorDTO dto,
                                                  Principal principal) {

        // 🔥 JWT gives email → principal.getName()
        String userEmail = principal.getName();

        Doctor doctor = DoctorMapper.toEntity(dto, userEmail);
        Doctor saved = doctorService.save(doctor);

        return ResponseEntity.ok(DoctorMapper.toDto(saved));
    }

    /** ⭐ GET ONLY DOCTORS CREATED BY THIS AUTHENTICATED DOCTOR */
    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors(Principal principal) {

        String userEmail = principal.getName();

        List<DoctorDTO> doctors = doctorService.getAll(userEmail)
                .stream()
                .map(DoctorMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(doctors);
    }

    /** ⭐ GET DOCTOR BY ID WITH OWNERSHIP VALIDATION */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id,
                                                   Principal principal) {

        String userEmail = principal.getName();
        Doctor doctor = doctorService.getById(id, userEmail);

        return ResponseEntity.ok(DoctorMapper.toDto(doctor));
    }

    /** ⭐ UPDATE DOCTOR PROFILE WITH OWNERSHIP VALIDATION */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO> updateDoctor(@PathVariable Long id,
                                                  @Valid @RequestBody DoctorDTO dto,
                                                  Principal principal) {

        String userEmail = principal.getName();
        Doctor updated = doctorService.update(id, DoctorMapper.toEntity(dto, userEmail), userEmail);

        return ResponseEntity.ok(DoctorMapper.toDto(updated));
    }

    /** ⭐ DELETE DOCTOR PROFILE WITH OWNERSHIP VALIDATION */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id,
                                             Principal principal) {

        String userEmail = principal.getName();
        doctorService.delete(id, userEmail);

        return ResponseEntity.noContent().build();
    }

    /** ⭐ GET DOCTORS WITH PAGINATION */
    @GetMapping("/search/paginated")
    public ResponseEntity<Page<DoctorDTO>> getDoctorsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            Principal principal) {

        String userEmail = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DoctorDTO> result = doctorService.getAllWithPagination(userEmail, pageable);

        return ResponseEntity.ok(result);
    }

    /** ⭐ SEARCH DOCTORS BY NAME */
    @GetMapping("/search/by-name")
    public ResponseEntity<Page<DoctorDTO>> searchDoctorsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            Principal principal) {

        String userEmail = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DoctorDTO> result = doctorService.searchByName(userEmail, name, pageable);

        return ResponseEntity.ok(result);
    }

    /** ⭐ SEARCH DOCTORS BY SPECIALIZATION */
    @GetMapping("/search/by-specialization")
    public ResponseEntity<Page<DoctorDTO>> searchDoctorsBySpecialization(
            @RequestParam String specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "specialization") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            Principal principal) {

        String userEmail = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DoctorDTO> result = doctorService.searchBySpecialization(userEmail, specialization, pageable);

        return ResponseEntity.ok(result);
    }
}
