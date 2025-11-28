package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.PatientDTO;
import com.carenexus.direct.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public static Patient toEntity(PatientDTO dto, String userEmail) {
        return Patient.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .userEmail(userEmail)
                .build();
    }

    public static PatientDTO toDto(Patient entity) {
        return PatientDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .build();
    }
}
