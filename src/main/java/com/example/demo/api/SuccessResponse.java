package com.example.demo.api;

import java.time.LocalDateTime;

public class SuccessResponse<T> {

    private boolean success;
    private T data;
    private LocalDateTime timestamp;

    private SuccessResponse(T data) {
        this.success = true;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> SuccessResponse<T> ok(T data) {
        return new SuccessResponse<>(data);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
