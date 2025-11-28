package com.carenexus.direct.repository;

import com.carenexus.direct.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserEmail(String email);

    Optional<Appointment> findByIdAndUserEmail(Long id, String email);

    /** Search appointments by user email with pagination */
    Page<Appointment> findByUserEmail(String email, Pageable pageable);

    /** Search appointments by reason and user email */
    @Query("SELECT a FROM Appointment a WHERE a.userEmail = :userEmail AND " +
           "LOWER(a.reason) LIKE LOWER(CONCAT('%', :reason, '%'))")
    Page<Appointment> searchByReasonAndUserEmail(@Param("userEmail") String userEmail,
                                                 @Param("reason") String reason,
                                                 Pageable pageable);

    /** Search appointments by date range */
    @Query("SELECT a FROM Appointment a WHERE a.userEmail = :userEmail AND " +
           "a.appointmentTime >= :startDate AND a.appointmentTime <= :endDate")
    Page<Appointment> searchByDateRangeAndUserEmail(@Param("userEmail") String userEmail,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);
}
