package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOtherServiceOrderRequest {
    private Long userId;
    private Long otherServiceId;
    private List<OrderItem> items;

    @Getter
    @Setter
    public static class OrderItem {
        private Long itemId;
        private Integer quantity;
    }
}
