package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.PatientDTO;
import com.carenexus.direct.model.Patient;

public class PatientMapper {
    public static PatientDTO toDto(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        return dto;
    }

    public static Patient toEntity(PatientDTO dto) {
        return Patient.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
    }
}
