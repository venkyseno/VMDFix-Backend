package com.example.demo.dto;

import java.math.BigDecimal;

public class WithdrawRequest {

    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
