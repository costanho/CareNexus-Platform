package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.AppointmentDTO;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.model.Doctor;
import com.carenexus.direct.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public static Appointment toEntity(AppointmentDTO dto, Doctor doctor, Patient patient, String userEmail) {
        return Appointment.builder()
                .id(dto.getId())
                .appointmentTime(dto.getAppointmentTime())
                .reason(dto.getReason())
                .userEmail(userEmail)
                .doctor(doctor)
                .patient(patient)
                .build();
    }

    public static AppointmentDTO toDto(Appointment entity) {
        return AppointmentDTO.builder()
                .id(entity.getId())
                .appointmentTime(entity.getAppointmentTime())
                .reason(entity.getReason())
                .build();
    }
}
