package com.example.demo.service;

import com.example.demo.model.AuditAction;
import com.example.demo.model.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(AuditAction action, Long userId, Long entityId) {

        AuditLog log = AuditLog.builder()
                .action(action)
                .performedBy(userId)
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .build();

        repository.save(log);
    }
}
