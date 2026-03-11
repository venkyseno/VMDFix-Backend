package com.example.demo.controller;

import com.example.demo.exception.BusinessException;
import com.example.demo.model.WithdrawalRequest;
import com.example.demo.model.WithdrawalStatus;
import com.example.demo.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/withdrawals")
@CrossOrigin
@RequiredArgsConstructor
public class AdminWithdrawController {

    private final WithdrawalService withdrawalService;

    // GET /api/admin/withdrawals          -> all
    // GET /api/admin/withdrawals?status=PENDING -> by status
    @GetMapping
    public List<WithdrawalRequest> getWithdrawals(
            @RequestParam(required = false) WithdrawalStatus status
    ) {
        if (status != null) {
            return withdrawalService.getByStatus(status);
        }
        return withdrawalService.getAll();
    }

    @GetMapping("/{id}")
    public WithdrawalRequest getById(@PathVariable Long id) {
        return withdrawalService.getById(id);
    }

    @PostMapping("/{id}/approve")
    public WithdrawalRequest approve(
            @PathVariable Long id,
            @RequestParam Long adminId
    ) {
        return withdrawalService.approve(id, adminId);
    }

    @PostMapping(value = "/{id}/reject", consumes = "application/json")
    public WithdrawalRequest reject(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestBody Map<String, String> body
    ) {
        if (!body.containsKey("reason") || body.get("reason").isBlank()) {
            throw new BusinessException("Rejection reason is mandatory");
        }
        return withdrawalService.reject(id, adminId, body.get("reason"));
    }
}
