package com.carenexus.direct.service;

import com.carenexus.direct.dto.DoctorDTO;
import com.carenexus.direct.exception.ForbiddenException;
import com.carenexus.direct.exception.NotFoundException;
import com.carenexus.direct.mapper.DoctorMapper;
import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    /** Get all doctors belonging to the authenticated user */
    public List<Doctor> getAll(String userEmail) {
        return doctorRepository.findByUserEmail(userEmail);
    }

    /** Get a doctor by ID without ownership check (for references) */
    public Doctor getById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
    }

    /** Get a doctor by ID with ownership validation */
    public Doctor getById(Long id, String userEmail) {
        return doctorRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
    }

    /** Update doctor with ownership validation */
    public Doctor update(Long id, Doctor doctorDetails, String userEmail) {
        Doctor doctor = getById(id, userEmail);  // Validates ownership
        doctor.setEmail(doctorDetails.getEmail());
        doctor.setPhone(doctorDetails.getPhone());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        return doctorRepository.save(doctor);
    }

    /** Delete doctor with ownership validation */
    public void delete(Long id, String userEmail) {
        Doctor doctor = getById(id, userEmail);  // Validates ownership
        doctorRepository.delete(doctor);
    }

    /** Get all doctors with pagination */
    public Page<DoctorDTO> getAllWithPagination(String userEmail, Pageable pageable) {
        return doctorRepository.findByUserEmail(userEmail, pageable)
                .map(DoctorMapper::toDto);
    }

    /** Search doctors by name with pagination */
    public Page<DoctorDTO> searchByName(String userEmail, String name, Pageable pageable) {
        return doctorRepository.searchByNameAndUserEmail(userEmail, name, pageable)
                .map(DoctorMapper::toDto);
    }

    /** Search doctors by specialization with pagination */
    public Page<DoctorDTO> searchBySpecialization(String userEmail, String specialization, Pageable pageable) {
        return doctorRepository.searchBySpecializationAndUserEmail(userEmail, specialization, pageable)
                .map(DoctorMapper::toDto);
    }
}
