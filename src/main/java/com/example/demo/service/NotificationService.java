package com.example.demo.service;

import com.example.demo.config.FirebaseConfig;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenRepository tokenRepo;
    private final NotificationConfigRepository configRepo;
    private final NotificationCampaignRepository campaignRepo;
    private final NotificationLogRepository logRepo;
    private final FcmService fcmService;
    private final TemplateProcessor templateProcessor;

    // ── Device token management ────────────────────────────────────────────────

    public DeviceToken registerToken(Long userId, String fcmToken, String platform) {

        if (fcmToken == null || fcmToken.isBlank()) {
            throw new RuntimeException("FCM token is required");
        }

        DeviceToken token = tokenRepo.findByFcmToken(fcmToken)
                .orElseGet(() -> DeviceToken.builder()
                        .fcmToken(fcmToken)
                        .createdAt(LocalDateTime.now())
                        .build());

        token.setUserId(userId);
        token.setPlatform(platform != null ? platform : "WEB");
        token.setUpdatedAt(LocalDateTime.now());

        return tokenRepo.save(token);
    }

    public void removeToken(String fcmToken) {
        if (fcmToken != null && !fcmToken.isBlank()) {
            tokenRepo.deleteByFcmToken(fcmToken);
        }
    }

    // ── Event-driven notification ──────────────────────────────────────────────

    public void handleEvent(String eventType, Long targetUserId, Map<String, String> variables) {
        try {
            if (!FirebaseConfig.isInitialized()) {
                System.out.printf("📲 [EVENT %s → user %s]: %s%n", eventType, targetUserId, variables);
                return;
            }

            List<NotificationConfig> configs = configRepo.findByEventType(eventType);

            for (NotificationConfig cfg : configs) {
                if (Boolean.FALSE.equals(cfg.getIsEnabled())) continue;
                if (!"PUSH".equalsIgnoreCase(cfg.getChannel())) continue;

                String title = templateProcessor.process(cfg.getTitleTemplate(), variables);
                String body = templateProcessor.process(cfg.getBodyTemplate(), variables);

                List<String> tokens = tokenRepo.findByUserId(targetUserId)
                        .stream()
                        .map(DeviceToken::getFcmToken)
                        .filter(Objects::nonNull)
                        .toList();

                for (String token : tokens) {
                    boolean ok = false;

                    try {
                        ok = fcmService.sendToToken(token, title, body);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    logRepo.save(NotificationLog.builder()
                            .userId(targetUserId)
                            .fcmToken(token)
                            .title(title)
                            .body(body)
                            .status(ok ? "SENT" : "FAILED")
                            .eventType(eventType)
                            .sentAt(LocalDateTime.now())
                            .build());
                }
            }

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 prevents crash
        }
    }

    // ── Broadcast / Campaign ───────────────────────────────────────────────────

    public NotificationCampaign sendCampaign(Long campaignId) {

        NotificationCampaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found: " + campaignId));

        List<DeviceToken> allTokens = tokenRepo.findAll();

        List<String> tokens = allTokens.stream()
                .map(DeviceToken::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .collect(Collectors.toList());

        int sent = 0;

        try {
            if (FirebaseConfig.isInitialized()) {
                sent = fcmService.sendToTokens(tokens, campaign.getTitle(), campaign.getBody());
            } else {
                System.out.printf("📢 [CAMPAIGN %s]: Firebase not configured, simulating send%n",
                        campaign.getTitle());
                sent = tokens.size();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (DeviceToken dt : allTokens) {
            logRepo.save(NotificationLog.builder()
                    .userId(dt.getUserId())
                    .fcmToken(dt.getFcmToken())
                    .title(campaign.getTitle())
                    .body(campaign.getBody())
                    .status("SENT")
                    .campaignId(campaignId)
                    .sentAt(LocalDateTime.now())
                    .build());
        }

        campaign.setStatus("SENT");
        campaign.setSentAt(LocalDateTime.now());
        campaign.setRecipientCount(sent);

        return campaignRepo.save(campaign);
    }

    public NotificationCampaign createCampaign(String title, String body, String targetType) {
        return campaignRepo.save(NotificationCampaign.builder()
                .title(title)
                .body(body)
                .targetType(targetType != null ? targetType : "ALL_USERS")
                .status("DRAFT")
                .createdAt(LocalDateTime.now())
                .build());
    }

    public List<NotificationCampaign> getAllCampaigns() {
        return campaignRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<NotificationConfig> getAllConfigs() {
        return configRepo.findAll();
    }

    public NotificationConfig saveConfig(NotificationConfig cfg) {
        return configRepo.save(cfg);
    }

    public long getDeviceCount() {
        return tokenRepo.count();
    }

    // ── Legacy stubs ───────────────────────────────────────────────────────────

    public void notifyWorker(String phone, String message) {
        System.out.printf("📲 [WORKER → %s]: %s%n", phone, message);
    }

    public void notifyUser(String userId, String message) {
        System.out.printf("📲 [USER → %s]: %s%n", userId, message);
    }

    public void notifyAdmin(String message) {
        System.out.printf("📲 [ADMIN]: %s%n", message);
    }
}