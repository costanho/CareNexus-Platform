package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.MessageDTO;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.model.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public static Message toEntity(MessageDTO dto, Appointment appointment, String sender) {
        return Message.builder()
                .id(dto.getId())
                .recipient(dto.getRecipient())
                .content(dto.getContent())
                .timestamp(java.time.LocalDateTime.now())
                .sender(sender)
                .appointment(appointment)
                .build();
    }

    public static MessageDTO toDto(Message entity) {
        return MessageDTO.builder()
                .id(entity.getId())
                .appointmentId(entity.getAppointment() != null ? entity.getAppointment().getId() : null)
                .sender(entity.getSender())
                .recipient(entity.getRecipient())
                .content(entity.getContent())
                .build();
    }
}
