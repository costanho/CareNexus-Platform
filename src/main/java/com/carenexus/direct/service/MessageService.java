package com.carenexus.direct.service;


import com.carenexus.direct.model.Message;
import com.carenexus.direct.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message save(Message message) {
        log.info("💬 Saving new message from {} to {}",
                message.getSender(), message.getRecipient());
        return messageRepository.save(message);
    }

    public List<Message> getAll() {
        log.debug("Fetching all messages from the database");
        return messageRepository.findAll();
    }

   public List<Message> getMessagesByAppointmentId(Long appointmentId) {
        log.info("Fetching messages for appointment ID={}", appointmentId);
        return messageRepository.findByAppointmentId(appointmentId);
    }

    public void deleteById(Long id) {
        log.warn("Deleting message with ID={}", id);
        messageRepository.deleteById(id);
    }
}
