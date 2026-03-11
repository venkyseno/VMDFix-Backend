package com.example.demo.exception;

public class CashbackNotAllowedException extends BusinessException {

    public CashbackNotAllowedException(String message) {
        super(message);
    }
}
