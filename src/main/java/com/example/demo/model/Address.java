package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    @Column(nullable = false, length = 500)
    private String addressLine;
    @Column(nullable = false)
    private String city;
    private String landmark;
    private Boolean primaryAddress;
}
