package com.carenexus.direct.repository;

import com.carenexus.direct.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByUserEmail(String userEmail);

    Optional<Patient> findByIdAndUserEmail(Long id, String userEmail);

    /** Search patients by user email with pagination */
    Page<Patient> findByUserEmail(String userEmail, Pageable pageable);

    /** Search patients by name and user email */
    @Query("SELECT p FROM Patient p WHERE p.userEmail = :userEmail AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Patient> searchByNameAndUserEmail(@Param("userEmail") String userEmail,
                                           @Param("name") String name,
                                           Pageable pageable);

    /** Search patients by email and user email */
    @Query("SELECT p FROM Patient p WHERE p.userEmail = :userEmail AND " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<Patient> searchByEmailAndUserEmail(@Param("userEmail") String userEmail,
                                            @Param("email") String email,
                                            Pageable pageable);
}
