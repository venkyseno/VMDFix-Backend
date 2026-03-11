package com.example.demo.repository;

import com.example.demo.model.CaseStatus;
import com.example.demo.model.ServiceCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceCaseRepository extends JpaRepository<ServiceCase, Long> {

    List<ServiceCase> findByWorkerId(Long workerId);

    List<ServiceCase> findByAssistedByUserIdOrderByCreatedAtDesc(Long userId);

    List<ServiceCase> findByStatus(CaseStatus status);
}

