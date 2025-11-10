package com.carenexus.direct.controller;

import com.carenexus.direct.dto.MessageDTO;
import com.carenexus.direct.mapper.MessageMapper;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.model.Message;

import com.carenexus.direct.service.AppointmentService;
import com.carenexus.direct.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor

public class MessageController {
    private final MessageService messageService;
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(@Valid @RequestBody MessageDTO dto) {
        Appointment appointment = appointmentService.getById(dto.getAppointmentId());
        Message message = MessageMapper.toEntity(dto, appointment);
        Message saved = messageService.save(message);
        return ResponseEntity.ok(MessageMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAllMessages() {
        List<MessageDTO> messages = messageService.getAll()
                .stream()
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByAppointment(@PathVariable Long appointmentId) {
        List<MessageDTO> messages = messageService.getMessagesByAppointmentId(appointmentId)
                .stream()
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
