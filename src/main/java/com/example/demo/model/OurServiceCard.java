package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "our_service_cards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OurServiceCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String price;
    private String gradient;
    private String imageUrl;
    private Boolean active;
    private Integer sortOrder;
}
