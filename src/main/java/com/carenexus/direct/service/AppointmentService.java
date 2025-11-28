package com.carenexus.direct.service;

import com.carenexus.direct.dto.AppointmentDTO;
import com.carenexus.direct.exception.NotFoundException;
import com.carenexus.direct.mapper.AppointmentMapper;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    /** Get all appointments belonging to the authenticated user */
    public List<Appointment> getAllByUser(String userEmail) {
        return appointmentRepository.findByUserEmail(userEmail);
    }

    /** Get appointment by ID with ownership validation */
    public Appointment getByIdAndUser(Long id, String userEmail) {
        return appointmentRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
    }

    /** Update appointment with ownership validation */
    public Appointment update(Long id, Appointment appointmentDetails, String userEmail) {
        Appointment appointment = getByIdAndUser(id, userEmail);  // Validates ownership
        appointment.setAppointmentTime(appointmentDetails.getAppointmentTime());
        appointment.setReason(appointmentDetails.getReason());
        return appointmentRepository.save(appointment);
    }

    /** Delete appointment with ownership validation */
    public void delete(Long id, String userEmail) {
        Appointment appt = getByIdAndUser(id, userEmail);
        appointmentRepository.delete(appt);
    }

    /** Get all appointments with pagination */
    public Page<AppointmentDTO> getAllWithPagination(String userEmail, Pageable pageable) {
        return appointmentRepository.findByUserEmail(userEmail, pageable)
                .map(AppointmentMapper::toDto);
    }

    /** Search appointments by reason with pagination */
    public Page<AppointmentDTO> searchByReason(String userEmail, String reason, Pageable pageable) {
        return appointmentRepository.searchByReasonAndUserEmail(userEmail, reason, pageable)
                .map(AppointmentMapper::toDto);
    }

    /** Search appointments by date range with pagination */
    public Page<AppointmentDTO> searchByDateRange(String userEmail, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return appointmentRepository.searchByDateRangeAndUserEmail(userEmail, startDate, endDate, pageable)
                .map(AppointmentMapper::toDto);
    }
}
