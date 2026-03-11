package com.example.demo.exception;

public class PaymentNotConfirmedException extends BusinessException {

    public PaymentNotConfirmedException() {
        super("Payment not confirmed. Case cannot be closed.");
    }
}
