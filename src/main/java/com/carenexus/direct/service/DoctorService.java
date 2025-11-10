package com.carenexus.direct.service;


import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor save(Doctor doctor) {
        log.info("👨‍⚕️ Saving doctor: {}", doctor.getName());
        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAll() {
        log.debug("Fetching all doctors from the database");
        return doctorRepository.findAll();
    }

    public Doctor getById(Long id) {
        log.info("Fetching doctor with ID={}", id);
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID=" + id));
    }

    public void deleteById(Long id) {
        log.warn("Deleting doctor record with ID={}", id);
        doctorRepository.deleteById(id);
    }
}
