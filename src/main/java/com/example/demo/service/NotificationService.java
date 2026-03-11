package com.example.demo.service;

import org.springframework.stereotype.Service;

/**
 * Handles all outbound notifications.
 * Phase 1: Console logging only.
 * Phase 2: Integrate Twilio / Meta WhatsApp Cloud API.
 */
@Service
public class NotificationService {

    public void notifyWorker(String phone, String message) {
        log("WORKER", phone, message);
    }

    public void notifyUser(String userId, String message) {
        log("USER", userId, message);
    }

    public void notifyAdmin(String message) {
        log("ADMIN", "admin", message);
    }

    private void log(String recipient, String target, String message) {
        System.out.printf("📲 [%s → %s]: %s%n", recipient, target, message);
    }
}
