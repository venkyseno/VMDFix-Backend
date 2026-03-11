package com.example.demo.controller;

import com.example.demo.dto.WithdrawRequest;
import com.example.demo.model.WithdrawalRequest;
import com.example.demo.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/{userId}/withdraw")
    public WithdrawalRequest withdraw(
            @PathVariable Long userId,
            @RequestBody WithdrawRequest request
    ) {
        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        return withdrawalService.requestWithdrawal(userId, request.getAmount());
    }
}
