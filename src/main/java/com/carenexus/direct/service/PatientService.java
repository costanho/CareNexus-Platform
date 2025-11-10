package com.carenexus.direct.service;

import com.carenexus.direct.model.Patient;
import com.carenexus.direct.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;


    public Patient save(Patient patient) {
        log.info("🧍 Saving new patient: {}", patient.getName());
        return patientRepository.save(patient);
    }

    public List<Patient> getAll() {
        log.debug("Fetching all patients from the database");
        return patientRepository.findAll();
    }

    public Patient getById(Long id) {
        log.info("Fetching patient with ID={}", id);
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID=" + id));
    }

    public void deleteById(Long id) {
        log.warn("Deleting patient with ID={}", id);
        patientRepository.deleteById(id);
    }
}
