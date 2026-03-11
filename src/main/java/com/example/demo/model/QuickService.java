package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quick_services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuickService {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String price;
    private String gradient;
    private String imageUrl;
    private String tag;
    private String bookings;
    private Double rating;
    private Boolean active;
    private Integer sortOrder;
}
