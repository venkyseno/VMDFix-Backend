package com.example.demo.service;

import com.example.demo.dto.CloseCaseRequest;
import com.example.demo.dto.CreateCaseRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.ServiceCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCaseService {

    private final ServiceCaseRepository repository;
    private final WalletService walletService;
    private final AuditService auditService;

    public ServiceCase createCase(CreateCaseRequest request) {
        ServiceCase serviceCase = ServiceCase.builder()
                .serviceId(request.getServiceId())
                .description(request.getDescription())
                .customerPhone(request.getCustomerPhone())
                .assistedByUserId(request.getAssistedByUserId())
                .attachmentUrl(request.getAttachmentUrl())
                .status(CaseStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        ServiceCase saved = repository.save(serviceCase);
        auditService.log(AuditAction.CASE_CREATED, request.getAssistedByUserId(), saved.getId());
        return saved;
    }

    public List<ServiceCase> getAllCases() {
        return repository.findAll();
    }

    public ServiceCase getCaseById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Case not found: " + id));
    }

    public List<ServiceCase> getCasesByUser(Long userId) {
        return repository.findByAssistedByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ServiceCase> getCasesByWorker(Long workerId) {
        return repository.findByWorkerId(workerId);
    }

    public ServiceCase closeCase(Long caseId, CloseCaseRequest request, Long adminId) {
        ServiceCase serviceCase = getCaseById(caseId);

        if (serviceCase.getStatus() == CaseStatus.CLOSED) {
            return serviceCase;
        }

        if (serviceCase.getStatus() != CaseStatus.WORK_DONE) {
            throw new BusinessException("Case must be WORK_DONE before closing");
        }

        serviceCase.setServiceAmount(request.getServiceAmount());
        serviceCase.setStatus(CaseStatus.CLOSED);
        ServiceCase saved = repository.save(serviceCase);

        if (saved.getAssistedByUserId() != null) {
            BigDecimal cashback = request.getServiceAmount().multiply(BigDecimal.valueOf(0.10));
            walletService.creditCashback(saved.getAssistedByUserId(), saved.getId(), cashback);
        }

        auditService.log(AuditAction.CASE_CLOSED, adminId, saved.getId());
        return saved;
    }
}
