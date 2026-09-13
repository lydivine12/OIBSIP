package com.library.service;

import com.library.model.ContactMessage;
import com.library.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    public ContactMessage submit(String name, String email, String message) {
        return contactMessageRepository.save(new ContactMessage(name, email, message));
    }

    public List<ContactMessage> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public void markResolved(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + id));
        message.setResolved(true);
        contactMessageRepository.save(message);
    }
}
