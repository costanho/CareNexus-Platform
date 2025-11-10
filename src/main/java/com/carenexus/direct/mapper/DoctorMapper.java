package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.DoctorDTO;
import com.carenexus.direct.model.Doctor;

public class DoctorMapper {
    public static DoctorDTO toDto(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setEmail(doctor.getEmail());
        dto.setPhone(doctor.getPhone());
        return dto;
    }

    public static Doctor toEntity(DoctorDTO dto) {
        return Doctor.builder()
                .id(dto.getId())
                .name(dto.getName())
                .specialization(dto.getSpecialization())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
    }
}
