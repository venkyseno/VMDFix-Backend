package com.example.demo.repository;

import com.example.demo.model.WithdrawalRequest;
import com.example.demo.model.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRepository
        extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByStatus(WithdrawalStatus status);
}
