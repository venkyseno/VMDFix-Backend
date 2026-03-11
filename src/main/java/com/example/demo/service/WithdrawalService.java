package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.InvalidLifecycleTransitionException;
import com.example.demo.exception.InvalidServiceException;
import com.example.demo.model.*;
import com.example.demo.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WalletService walletService;
    private final WithdrawalRepository withdrawalRepository;
    private final AuditService auditService;

    private static final BigDecimal MIN_WITHDRAWAL = BigDecimal.valueOf(500);

    /* =========================
       USER WITHDRAWAL REQUEST
       ========================= */
    @Transactional
    public WithdrawalRequest requestWithdrawal(Long userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(MIN_WITHDRAWAL) < 0) {
            throw new BusinessException(
                    "Minimum withdrawal amount is ₹500"
            );
        }

        UserWallet wallet = walletService.getWallet(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(
                    "Insufficient wallet balance"
            );
        }

        WithdrawalRequest request = new WithdrawalRequest();
        request.setUserId(userId);
        request.setAmount(amount);
        request.setStatus(WithdrawalStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());

        WithdrawalRequest saved = withdrawalRepository.save(request);

        auditService.log(
                AuditAction.WITHDRAWAL_REQUESTED,
                userId,
                saved.getId()
        );

        return saved;
    }

    /* =========================
       ADMIN APPROVE WITHDRAWAL
       ========================= */
    @Transactional
    public WithdrawalRequest approve(Long withdrawalId, Long adminId) {

        WithdrawalRequest request = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new InvalidServiceException(
                        "Withdrawal not found"
                ));

        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new InvalidLifecycleTransitionException(
                    "Only PENDING withdrawals can be approved"
            );
        }

        UserWallet wallet = walletService.getWallet(request.getUserId());

        walletService.debit(wallet, request.getAmount(), request.getId());

        request.setStatus(WithdrawalStatus.APPROVED);
        request.setProcessedBy(adminId);
        request.setProcessedAt(LocalDateTime.now());

        WithdrawalRequest saved = withdrawalRepository.save(request);

        auditService.log(
                AuditAction.WITHDRAWAL_APPROVED,
                adminId,
                saved.getId()
        );

        return saved;
    }

    /* =========================
       ADMIN REJECT WITHDRAWAL
       ========================= */
    @Transactional
    public WithdrawalRequest reject(Long withdrawalId, Long adminId, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new BusinessException(
                    "Rejection reason is mandatory"
            );
        }

        WithdrawalRequest request = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new InvalidServiceException(
                        "Withdrawal not found"
                ));

        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new InvalidLifecycleTransitionException(
                    "Only PENDING withdrawals can be rejected"
            );
        }

        request.setStatus(WithdrawalStatus.REJECTED);
        request.setProcessedBy(adminId);
        request.setProcessedAt(LocalDateTime.now());

        WithdrawalRequest saved = withdrawalRepository.save(request);

        auditService.log(
                AuditAction.WITHDRAWAL_REJECTED,
                adminId,
                saved.getId()
        );

        return saved;
    }

    /* =========================
       ADMIN READ APIs
       ========================= */
    public List<WithdrawalRequest> getAll() {
        return withdrawalRepository.findAll();
    }

    public List<WithdrawalRequest> getByStatus(WithdrawalStatus status) {
        return withdrawalRepository.findByStatus(status);
    }

    public WithdrawalRequest getById(Long id) {
        return withdrawalRepository.findById(id)
                .orElseThrow(() -> new InvalidServiceException(
                        "Withdrawal not found"
                ));
    }
}
