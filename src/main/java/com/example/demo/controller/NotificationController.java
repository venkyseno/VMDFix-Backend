package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ── Device token registration ──────────────────────────────────────────────

    @PostMapping("/token")
    public Object registerToken(@RequestBody Map<String, String> body) {
        try {
            System.out.println("Incoming token request: " + body);

            Long userId = null;
            try {
                String userIdStr = body.get("userId");
                if (userIdStr != null && !userIdStr.isBlank()) {
                    userId = Long.parseLong(userIdStr);
                }
            } catch (Exception e) {
                userId = null;
            }

            String token = body.get("fcmToken");
            String platform = body.getOrDefault("platform", "WEB");

            if (token == null || token.isBlank()) {
                return Map.of("error", "fcmToken is required");
            }

            return notificationService.registerToken(userId, token, platform);

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 IMPORTANT
            return Map.of("error", e.getMessage());
        }
    }

    @DeleteMapping("/token")
    public Object removeToken(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("fcmToken");
            if (token == null || token.isBlank()) {
                return Map.of("error", "fcmToken is required");
            }

            notificationService.removeToken(token);
            return Map.of("status", "removed");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    // ── Notification configs (admin) ──────────────────────────────────────────

    @GetMapping("/configs")
    public Object getConfigs() {
        try {
            return notificationService.getAllConfigs();
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/configs")
    public Object saveConfig(@RequestBody NotificationConfig config) {
        try {
            return notificationService.saveConfig(config);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    // ── Campaigns (admin) ─────────────────────────────────────────────────────

    @GetMapping("/campaigns")
    public Object getCampaigns() {
        try {
            return notificationService.getAllCampaigns();
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/campaigns")
    public Object createCampaign(@RequestBody Map<String, String> body) {
        try {
            return notificationService.createCampaign(
                body.get("title"),
                body.get("body"),
                body.get("targetType")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/campaigns/{id}/send")
    public Object sendCampaign(@PathVariable Long id) {
        try {
            return notificationService.sendCampaign(id);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public Object getStats() {
        try {
            boolean firebaseReady = false;

            try {
                firebaseReady = com.example.demo.config.FirebaseConfig.isInitialized();
            } catch (Exception e) {
                firebaseReady = false;
            }

            return Map.of(
                "deviceCount", notificationService.getDeviceCount(),
                "firebaseReady", firebaseReady
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}