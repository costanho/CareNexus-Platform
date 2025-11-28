package com.carenexus.direct.controller;

import com.carenexus.direct.dto.PatientDTO;
import com.carenexus.direct.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getMyPatients(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(patientService.getAllPatients(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(patientService.getPatientById(id, user.getUsername(), isAdmin));
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO dto,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(patientService.createPatient(dto, user.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id,
                                                    @RequestBody PatientDTO dto,
                                                    @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        patientService.deletePatient(id, user.getUsername(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<Page<PatientDTO>> getPatientsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @AuthenticationPrincipal UserDetails user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PatientDTO> result = patientService.getAllWithPagination(user.getUsername(), pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<Page<PatientDTO>> searchPatientsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @AuthenticationPrincipal UserDetails user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PatientDTO> result = patientService.searchByName(user.getUsername(), name, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-email")
    public ResponseEntity<Page<PatientDTO>> searchPatientsByEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "email") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @AuthenticationPrincipal UserDetails user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PatientDTO> result = patientService.searchByEmail(user.getUsername(), email, pageable);

        return ResponseEntity.ok(result);
    }
}
