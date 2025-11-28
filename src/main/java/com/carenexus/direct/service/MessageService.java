package com.carenexus.direct.service;

import com.carenexus.direct.dto.MessageDTO;
import com.carenexus.direct.exception.NotFoundException;
import com.carenexus.direct.mapper.MessageMapper;
import com.carenexus.direct.model.Message;
import com.carenexus.direct.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    /** Get all messages belonging to the authenticated user */
    public List<Message> getAllForUser(String email) {
        return messageRepository.findByUserEmail(email);
    }

    /** Get messages for a specific appointment with ownership validation */
    public List<Message> getByAppointment(Long appointmentId, String email) {
        return messageRepository.findByAppointmentIdAndUserEmail(appointmentId, email);
    }

    /** Get message by ID with ownership validation */
    public Message getByIdAndUser(Long id, String email) {
        return messageRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Message not found"));
    }

    /** Update message with ownership validation */
    public Message update(Long id, Message messageDetails, String userEmail) {
        Message message = getByIdAndUser(id, userEmail);  // Validates ownership
        message.setContent(messageDetails.getContent());
        message.setTimestamp(messageDetails.getTimestamp());
        return messageRepository.save(message);
    }

    /** Delete message with ownership validation */
    public void delete(Long id, String userEmail) {
        Message message = getByIdAndUser(id, userEmail);
        messageRepository.delete(message);
    }

    /** Get all messages with pagination */
    public Page<MessageDTO> getAllWithPagination(String userEmail, Pageable pageable) {
        return messageRepository.findByUserEmail(userEmail, pageable)
                .map(MessageMapper::toDto);
    }

    /** Get messages for specific appointment with pagination */
    public Page<MessageDTO> getAppointmentMessagesWithPagination(Long appointmentId, String userEmail, Pageable pageable) {
        return messageRepository.findByAppointmentIdAndUserEmail(appointmentId, userEmail, pageable)
                .map(MessageMapper::toDto);
    }

    /** Search messages by content with pagination */
    public Page<MessageDTO> searchByContent(String userEmail, String content, Pageable pageable) {
        return messageRepository.searchByContentAndUserEmail(userEmail, content, pageable)
                .map(MessageMapper::toDto);
    }

    /** Search messages by date range with pagination */
    public Page<MessageDTO> searchByDateRange(String userEmail, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return messageRepository.searchByDateRangeAndUserEmail(userEmail, startDate, endDate, pageable)
                .map(MessageMapper::toDto);
    }
}
