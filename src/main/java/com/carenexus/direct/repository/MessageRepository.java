package com.carenexus.direct.repository;

import com.carenexus.direct.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByUserEmail(String userEmail);

    List<Message> findByAppointmentIdAndUserEmail(Long appointmentId, String userEmail);

    Optional<Message> findByIdAndUserEmail(Long id, String userEmail);

    /** Search messages by user email with pagination */
    Page<Message> findByUserEmail(String userEmail, Pageable pageable);

    /** Search messages by appointment with pagination */
    Page<Message> findByAppointmentIdAndUserEmail(Long appointmentId, String userEmail, Pageable pageable);

    /** Search messages by content and user email */
    @Query("SELECT m FROM Message m WHERE m.userEmail = :userEmail AND " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    Page<Message> searchByContentAndUserEmail(@Param("userEmail") String userEmail,
                                              @Param("content") String content,
                                              Pageable pageable);

    /** Search messages by date range */
    @Query("SELECT m FROM Message m WHERE m.userEmail = :userEmail AND " +
           "m.timestamp >= :startDate AND m.timestamp <= :endDate")
    Page<Message> searchByDateRangeAndUserEmail(@Param("userEmail") String userEmail,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);
}
