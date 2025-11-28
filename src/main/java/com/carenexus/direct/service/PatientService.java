package com.carenexus.direct.service;

import com.carenexus.direct.dto.PatientDTO;
import com.carenexus.direct.exception.ForbiddenException;
import com.carenexus.direct.exception.NotFoundException;
import com.carenexus.direct.mapper.PatientMapper;
import com.carenexus.direct.model.Patient;
import com.carenexus.direct.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    /** Get all patients belonging to the authenticated user */
    public List<Patient> getAll(String userEmail) {
        return patientRepository.findByUserEmail(userEmail);
    }

    /** Get a patient by ID without ownership check (for references) */
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }

    /** Get a patient by ID with ownership validation */
    public Patient getById(Long id, String userEmail) {
        return patientRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }

    /** Update patient with ownership validation */
    public Patient update(Long id, Patient patientDetails, String userEmail) {
        Patient patient = getById(id, userEmail);  // Validates ownership
        patient.setPhone(patientDetails.getPhone());
        patient.setName(patientDetails.getName());
        patient.setEmail(patientDetails.getEmail());
        return patientRepository.save(patient);
    }

    /** Delete patient with ownership validation */
    public void delete(Long id, String userEmail) {
        Patient patient = getById(id, userEmail);  // Validates ownership
        patientRepository.delete(patient);
    }

    /** Get all patients with DTO mapping */
    public List<PatientDTO> getAllPatients(String userEmail) {
        return getAll(userEmail)
                .stream()
                .map(PatientMapper::toDto)
                .collect(Collectors.toList());
    }

    /** Get patient by ID with DTO mapping and optional admin override */
    public PatientDTO getPatientById(Long id, String userEmail, boolean isAdmin) {
        Patient patient;
        if (isAdmin) {
            patient = patientRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Patient not found"));
        } else {
            patient = getById(id, userEmail);
        }
        return PatientMapper.toDto(patient);
    }

    /** Create patient with ownership */
    public PatientDTO createPatient(PatientDTO dto, String userEmail) {
        Patient patient = PatientMapper.toEntity(dto, userEmail);
        Patient saved = save(patient);
        return PatientMapper.toDto(saved);
    }

    /** Update patient with ownership validation */
    public PatientDTO updatePatient(Long id, PatientDTO dto, String userEmail) {
        Patient patient = update(id, PatientMapper.toEntity(dto, userEmail), userEmail);
        return PatientMapper.toDto(patient);
    }

    /** Delete patient with optional admin override */
    public void deletePatient(Long id, String userEmail, boolean isAdmin) {
        if (isAdmin) {
            patientRepository.deleteById(id);
        } else {
            delete(id, userEmail);
        }
    }

    /** Get all patients with pagination */
    public Page<PatientDTO> getAllWithPagination(String userEmail, Pageable pageable) {
        return patientRepository.findByUserEmail(userEmail, pageable)
                .map(PatientMapper::toDto);
    }

    /** Search patients by name with pagination */
    public Page<PatientDTO> searchByName(String userEmail, String name, Pageable pageable) {
        return patientRepository.searchByNameAndUserEmail(userEmail, name, pageable)
                .map(PatientMapper::toDto);
    }

    /** Search patients by email with pagination */
    public Page<PatientDTO> searchByEmail(String userEmail, String email, Pageable pageable) {
        return patientRepository.searchByEmailAndUserEmail(userEmail, email, pageable)
                .map(PatientMapper::toDto);
    }
}
