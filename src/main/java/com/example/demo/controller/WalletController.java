package com.example.demo.controller;

import com.example.demo.model.UserWallet;
import com.example.demo.model.WalletLedger;
import com.example.demo.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // ✅ Get wallet balance
    @GetMapping("/{userId}")
    public UserWallet getWallet(@PathVariable Long userId) {
        return walletService.getWallet(userId);
    }

    // ✅ Get wallet ledger
    @GetMapping("/{userId}/ledger")
    public List<WalletLedger> getLedger(@PathVariable Long userId) {
        return walletService.getLedger(userId);
    }
}
