package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.AppointmentDTO;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.model.Patient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentMapper {

    public static AppointmentDTO toDto(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setAppointmentTime(appointment.getAppointmentTime()); // ✅ No need to format
        dto.setReason(appointment.getReason());
        return dto;
    }

    public static Appointment toEntity(AppointmentDTO dto, Doctor doctor, Patient patient) {
        return Appointment.builder()
                .id(dto.getId())
                .doctor(doctor)
                .patient(patient)
                .appointmentTime(dto.getAppointmentTime()) // ✅ Directly use LocalDateTime
                .reason(dto.getReason())
                .build();
    }
}
