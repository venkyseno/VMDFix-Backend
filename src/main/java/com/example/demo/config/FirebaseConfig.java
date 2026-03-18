package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static boolean initialized = false;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @PostConstruct
    public void initFirebase() {
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            System.out.println("⚠️  Firebase not configured — set FIREBASE_SERVICE_ACCOUNT_JSON env var to enable push notifications");
            return;
        }
        try {
            // Use fully qualified names to avoid import resolution issues
            java.util.List<?> apps = com.google.firebase.FirebaseApp.getApps();
            if (apps != null && !apps.isEmpty()) {
                initialized = true;
                return;
            }
            InputStream stream = new ByteArrayInputStream(
                serviceAccountJson.getBytes(StandardCharsets.UTF_8)
            );
            com.google.auth.oauth2.GoogleCredentials credentials =
                com.google.auth.oauth2.GoogleCredentials.fromStream(stream);
            com.google.firebase.FirebaseOptions options =
                com.google.firebase.FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            com.google.firebase.FirebaseApp.initializeApp(options);
            initialized = true;
            System.out.println("✅ Firebase initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Firebase init failed: " + e.getMessage());
            initialized = false;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
