package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "notifications_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventType;       // ORDER_CREATED, WORK_ASSIGNED, etc.
    private String targetRole;      // CUSTOMER, WORKER, ADMIN
    private String titleTemplate;
    private String bodyTemplate;
    private String channel;         // PUSH, SMS, EMAIL
    private Boolean isEnabled;
}
