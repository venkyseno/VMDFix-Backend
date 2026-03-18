package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notification_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String fcmToken;
    private String title;
    @Column(length = 1000)
    private String body;
    private String status;          // SENT, FAILED
    private String errorMessage;
    private Long campaignId;
    private String eventType;
    private LocalDateTime sentAt;
}
