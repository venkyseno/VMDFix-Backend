package com.example.demo.service;

import com.example.demo.config.FirebaseConfig;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

import com.google.firebase.messaging.*;

@Service
public class FcmService {

    /** Send to a single FCM token. Returns true if successful. */
    public boolean sendToToken(String token, String title, String body) {

        if (!FirebaseConfig.isInitialized()) {
            System.out.printf("📲 [FCM SIMULATED → %s]: %s — %s%n", token, title, body);
            return true;
        }

        try {

            // 🔥 DATA-ONLY MESSAGE (CRITICAL FIX)
            Message message = Message.builder()
                    .setToken(token)

                    // ✅ Required for web push priority
                    .setWebpushConfig(
                            WebpushConfig.builder()
                                    .putHeader("Urgency", "high")
                                    .build()
                    )

                    // ✅ ONLY DATA PAYLOAD (NO notification block)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("click_action", "/")
                    .putData("notificationId", String.valueOf(System.currentTimeMillis()))

                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            System.out.println("✅ FCM sent: " + response);
            return true;

        } catch (Exception e) {
            System.err.println("❌ FCM send failed: " + e.getMessage());
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

                // 🔥 MULTICAST DATA-ONLY MESSAGE
                MulticastMessage message = MulticastMessage.builder()
                        .addAllTokens(batch)

                        // ✅ Web push config
                        .setWebpushConfig(
                                WebpushConfig.builder()
                                        .putHeader("Urgency", "high")
                                        .build()
                        )

                        // ✅ DATA PAYLOAD ONLY
                        .putData("title", title)
                        .putData("body", body)
                        .putData("click_action", "/")
                        .putData("notificationId", String.valueOf(System.currentTimeMillis()))

                        .build();

                BatchResponse response =
                        FirebaseMessaging.getInstance().sendEachForMulticast(message);

                success += response.getSuccessCount();

            } catch (Exception e) {
                System.err.println("❌ FCM multicast failed: " + e.getMessage());
            }
        }

        return success;
    }

    /** Helper to split list into batches */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return parts;
    }
}