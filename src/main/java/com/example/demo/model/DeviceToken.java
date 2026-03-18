package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "device_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Column(length = 500, unique = true)
    private String fcmToken;
    private String platform; // WEB, ANDROID, IOS
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
