package com.example.demo.repository;

import com.example.demo.model.CommissionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {
}
