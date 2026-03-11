package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long serviceId;

    @Column(length = 500)
    private String description;

    private String customerPhone;

    private Long assistedByUserId;

    private Long workerId;

    @Column(length = 1000)
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    private CaseStatus status;

    @Enumerated(EnumType.STRING)
    private BookingMode bookingMode;

    private BigDecimal serviceAmount;

    private LocalDateTime createdAt;
}
