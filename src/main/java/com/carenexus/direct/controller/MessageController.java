package com.carenexus.direct.controller;

import com.carenexus.direct.dto.MessageDTO;
import com.carenexus.direct.mapper.MessageMapper;
import com.carenexus.direct.model.Appointment;
import com.carenexus.direct.model.Message;
import com.carenexus.direct.service.AppointmentService;
import com.carenexus.direct.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(
            @Valid @RequestBody MessageDTO dto,
            Principal principal
    ) {
        String email = principal.getName();

        Appointment appointment = appointmentService.getByIdAndUser(dto.getAppointmentId(), email);

        Message saved = messageService.save(
                MessageMapper.toEntity(dto, appointment, email)
        );

        return ResponseEntity.ok(MessageMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAllMessages(Principal principal) {
        String email = principal.getName();

        List<MessageDTO> list = messageService.getAllForUser(email)
                .stream()
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getMessageById(
            @PathVariable Long id,
            Principal principal
    ) {
        String email = principal.getName();
        var message = messageService.getByIdAndUser(id, email);
        return ResponseEntity.ok(MessageMapper.toDto(message));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByAppointment(
            @PathVariable Long appointmentId,
            Principal principal
    ) {
        String email = principal.getName();

        List<MessageDTO> list = messageService.getByAppointment(appointmentId, email)
                .stream().map(MessageMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageDTO> updateMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageDTO dto,
            Principal principal
    ) {
        String email = principal.getName();
        var updated = messageService.update(id, MessageMapper.toEntity(dto, null, email), email);
        return ResponseEntity.ok(MessageMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long id,
            Principal principal
    ) {
        messageService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<Page<MessageDTO>> getMessagesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            Principal principal) {

        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MessageDTO> result = messageService.getAllWithPagination(email, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-appointment/{appointmentId}")
    public ResponseEntity<Page<MessageDTO>> getMessagesByAppointmentWithPagination(
            @PathVariable Long appointmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            Principal principal) {

        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MessageDTO> result = messageService.getAppointmentMessagesWithPagination(appointmentId, email, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-content")
    public ResponseEntity<Page<MessageDTO>> searchMessagesByContent(
            @RequestParam String content,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            Principal principal) {

        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MessageDTO> result = messageService.searchByContent(email, content, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-date-range")
    public ResponseEntity<Page<MessageDTO>> searchMessagesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            Principal principal) {

        String email = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MessageDTO> result = messageService.searchByDateRange(email, startDate, endDate, pageable);

        return ResponseEntity.ok(result);
    }
}
