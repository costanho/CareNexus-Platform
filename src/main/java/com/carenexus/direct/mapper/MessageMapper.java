package com.carenexus.direct.mapper;

import com.carenexus.direct.dto.MessageDTO;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.model.Message;

import java.time.LocalDateTime;

public class MessageMapper {

    public static MessageDTO toDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSender(message.getSender());
        dto.setRecipient(message.getRecipient());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setAppointmentId(message.getAppointment().getId());
        return dto;
    }

    public static Message toEntity(MessageDTO dto, Appointment appointment) {
        return Message.builder()
                .id(dto.getId())
                .sender(dto.getSender())
                .recipient(dto.getRecipient())
                .content(dto.getContent())
                .timestamp(dto.getTimestamp())
                .appointment(appointment)
                .build();
    }
}
