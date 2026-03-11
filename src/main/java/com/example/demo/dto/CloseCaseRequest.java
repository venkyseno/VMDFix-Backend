package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CloseCaseRequest {

    private BigDecimal serviceAmount;

    private String paymentMode;

    private boolean paymentConfirmed;
}
