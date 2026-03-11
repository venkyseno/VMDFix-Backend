package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.UserWalletRepository;
import com.example.demo.repository.WalletLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserWalletRepository userWalletRepository;
    private final WalletLedgerRepository walletLedgerRepository;

    /* =========================
       GET WALLET
       ========================= */
    public UserWallet getWallet(Long userId) {

        return userWalletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserWallet wallet = new UserWallet();
                    wallet.setUserId(userId);
                    wallet.setBalance(BigDecimal.ZERO);
                    return userWalletRepository.save(wallet);
                });
    }
    

    /* =========================
       CREDIT (Cashback etc.)
       ========================= */
    @Transactional
    public void creditCashback(Long userId, Long referenceId, BigDecimal amount) {

           if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        UserWallet wallet = getWallet(userId);

        wallet.setBalance(wallet.getBalance().add(amount));
        userWalletRepository.save(wallet);

        WalletLedger ledger = WalletLedger.builder()
                .userId(userId)
                .amount(amount)
                .type(WalletLedgerType.CREDIT)
                .referenceId(referenceId)
                .createdAt(LocalDateTime.now())
                .build();

        walletLedgerRepository.save(ledger);
    }

    /* =========================
       DEBIT (Withdrawal)
       ========================= */
    @Transactional
    public void debit(UserWallet wallet, BigDecimal amount, Long referenceId) {

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        userWalletRepository.save(wallet);

        WalletLedger ledger = WalletLedger.builder()
                .userId(wallet.getUserId())
                .amount(amount)
                .type(WalletLedgerType.DEBIT)
                .referenceId(referenceId)
                .createdAt(LocalDateTime.now())
                .build();

        walletLedgerRepository.save(ledger);
    }

    /* =========================
       GET LEDGER
       ========================= */
    public List<WalletLedger> getLedger(Long userId) {
        return walletLedgerRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
