package com.example.demo.service;

import com.example.demo.config.FirebaseConfig;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

/**
 * Firebase Cloud Messaging service.
 * Uses reflection-safe checks so the app starts even when Firebase is not configured.
 */
@Service
public class FcmService {

    /** Send to a single FCM token. Returns true if successful. */
    public boolean sendToToken(String token, String title, String body) {
        if (!FirebaseConfig.isInitialized()) {
            System.out.printf("📲 [FCM SIMULATED → %s]: %s — %s%n", token, title, body);
            return true;
        }
        try {
            com.google.firebase.messaging.Message message =
                com.google.firebase.messaging.Message.builder()
                    .setToken(token)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                    .setWebpushConfig(
                        com.google.firebase.messaging.WebpushConfig.builder()
                            .setNotification(
                                com.google.firebase.messaging.WebpushNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setIcon("/icon.jpeg")
                                    .build())
                            .build())
                    .putData("click_action", "/")
                    .build();
            com.google.firebase.messaging.FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (Exception e) {
            System.err.println("FCM send failed for token " + token.substring(0, Math.min(10, token.length())) + "...: " + e.getMessage());
            return false;
        }
    }

    /** Send to multiple tokens. Returns count of successes. */
    public int sendToTokens(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) return 0;
        if (!FirebaseConfig.isInitialized()) {
            System.out.printf("📢 [FCM SIMULATED BULK → %d devices]: %s%n", tokens.size(), title);
            return tokens.size();
        }
        int success = 0;
        for (List<String> batch : partition(tokens, 500)) {
            try {
                com.google.firebase.messaging.MulticastMessage msg =
                    com.google.firebase.messaging.MulticastMessage.builder()
                        .addAllTokens(batch)
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                        .setWebpushConfig(
                            com.google.firebase.messaging.WebpushConfig.builder()
                                .setNotification(
                                    com.google.firebase.messaging.WebpushNotification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .setIcon("/icon.jpeg")
                                        .build())
                                .build())
                        .putData("click_action", "/")
                        .build();
                com.google.firebase.messaging.BatchResponse resp =
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().sendEachForMulticast(msg);
                success += resp.getSuccessCount();
            } catch (Exception e) {
                System.err.println("FCM multicast failed: " + e.getMessage());
            }
        }
        return success;
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size)
            parts.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        return parts;
    }
}
