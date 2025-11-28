package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.DoctorDTO;
import com.carenexus.direct.model.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public static Doctor toEntity(DoctorDTO dto, String userEmail) {
        return Doctor.builder()
                .id(dto.getId())
                .name(dto.getName())
                .specialization(dto.getSpecialization())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .userEmail(userEmail)
                .build();
    }

    public static DoctorDTO toDto(Doctor entity) {
        return DoctorDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .specialization(entity.getSpecialization())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .build();
    }
}
