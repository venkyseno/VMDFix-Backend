package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notifications_campaign")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationCampaign {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(length = 1000)
    private String body;
    private String targetType;      // ALL_USERS, CUSTOMERS, WORKERS
    private LocalDateTime scheduledTime;
    private String status;          // DRAFT, SENT
    private LocalDateTime sentAt;
    private Integer recipientCount;
    private LocalDateTime createdAt;
}
