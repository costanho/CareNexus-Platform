package com.carenexus.direct.repository;

import com.carenexus.direct.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByUserEmail(String userEmail);

    Optional<Doctor> findByIdAndUserEmail(Long id, String userEmail);

    /** Search doctors by user email with pagination */
    Page<Doctor> findByUserEmail(String userEmail, Pageable pageable);

    /** Search doctors by name and user email */
    @Query("SELECT d FROM Doctor d WHERE d.userEmail = :userEmail AND " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Doctor> searchByNameAndUserEmail(@Param("userEmail") String userEmail,
                                          @Param("name") String name,
                                          Pageable pageable);

    /** Search doctors by specialization and user email */
    @Query("SELECT d FROM Doctor d WHERE d.userEmail = :userEmail AND " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    Page<Doctor> searchBySpecializationAndUserEmail(@Param("userEmail") String userEmail,
                                                    @Param("specialization") String specialization,
                                                    Pageable pageable);
}
