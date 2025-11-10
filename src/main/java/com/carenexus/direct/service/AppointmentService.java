package com.carenexus.direct.service;

import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;


    public Appointment save(Appointment appointment) {
        log.info("🩺 Saving new appointment for Doctor ID={} and Patient ID={}",
                appointment.getDoctor().getId(),
                appointment.getPatient().getId());
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAll() {
        log.debug("Fetching all appointments from the database...");
        return appointmentRepository.findAll();
    }

    public Appointment getById(Long id) {
        log.info("Fetching appointment with ID={}", id);
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID=" + id));
    }

    public void deleteById(Long id) {
        log.warn("Deleting appointment with ID={}", id);
        appointmentRepository.deleteById(id);
    }
}
