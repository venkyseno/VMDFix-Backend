package com.example.demo.controller;

import com.example.demo.dto.CreateOtherServiceOrderRequest;
import com.example.demo.model.OtherServiceItem;
import com.example.demo.model.OtherServiceOrder;
import com.example.demo.repository.OtherServiceItemRepository;
import com.example.demo.repository.OtherServiceOrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/other-service-orders")
@CrossOrigin
@RequiredArgsConstructor
public class OtherServiceOrderController {
    private final OtherServiceOrderRepository orderRepository;
    private final OtherServiceItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    @PostMapping
    public OtherServiceOrder createOrder(@RequestBody CreateOtherServiceOrderRequest request) {
        if (request.getUserId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is required");
        if (request.getOtherServiceId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Other service is required");
        if (request.getItems() == null || request.getItems().isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOtherServiceOrderRequest.OrderItem item : request.getItems()) {
            OtherServiceItem dbItem = itemRepository.findById(item.getItemId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item not found"));
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
            total = total.add(dbItem.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        try {
            OtherServiceOrder order = OtherServiceOrder.builder()
                    .userId(request.getUserId())
                    .otherServiceId(request.getOtherServiceId())
                    .itemsJson(objectMapper.writeValueAsString(request.getItems()))
                    .totalAmount(total)
                    .status("CREATED")
                    .createdAt(LocalDateTime.now())
                    .build();
            return orderRepository.save(order);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid items");
        }
    }

    @GetMapping("/user/{userId}")
    public List<OtherServiceOrder> myOrders(@PathVariable Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
