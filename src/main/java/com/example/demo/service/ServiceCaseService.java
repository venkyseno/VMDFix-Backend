package com.example.demo.service;

import com.example.demo.dto.CloseCaseRequest;
import com.example.demo.dto.CreateCaseRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCaseService {

    private final ServiceCaseRepository repository;
    private final WalletService walletService;
    private final AuditService auditService;
    private final OurServiceCardRepository ourServiceCardRepository;
    private final QuickServiceRepository quickServiceRepository;
    private final AddressRepository addressRepository;
    private final NotificationService notificationService;

    /**
     * Resolve service name dynamically from DB (no hardcoded IDs).
     * Priority: OurServiceCard → QuickService → provided name → "Service #id"
     */
    private String resolveServiceName(Long serviceId, String providedName) {
        if (serviceId != null) {
            // Try OurServiceCard first
            var card = ourServiceCardRepository.findById(serviceId);
            if (card.isPresent()) return card.get().getName();

            // Try QuickService
            var qs = quickServiceRepository.findById(serviceId);
            if (qs.isPresent()) return qs.get().getName();
        }
        if (providedName != null && !providedName.isBlank()) return providedName;
        return serviceId != null ? "Service #" + serviceId : "Service";
    }

    private String resolveServiceImage(Long serviceId, String providedImage) {
        if (serviceId != null) {
            var card = ourServiceCardRepository.findById(serviceId);
            if (card.isPresent() && card.get().getImageUrl() != null) return card.get().getImageUrl();
            var qs = quickServiceRepository.findById(serviceId);
            if (qs.isPresent() && qs.get().getImageUrl() != null) return qs.get().getImageUrl();
        }
        return providedImage;
    }

    private String resolveBookingAddress(Long userId, String providedAddress) {
        if (providedAddress != null && !providedAddress.isBlank()) return providedAddress;
        if (userId != null) {
            var primary = addressRepository.findByUserIdAndPrimaryAddressTrue(userId);
            if (!primary.isEmpty()) {
                var addr = primary.get(0);
                return addr.getAddressLine() + (addr.getCity() != null ? ", " + addr.getCity() : "");
            }
        }
        return null;
    }

    public ServiceCase createCase(CreateCaseRequest request) {
        String resolvedName = resolveServiceName(request.getServiceId(), request.getServiceName());
        String resolvedImage = resolveServiceImage(request.getServiceId(), request.getServiceImageUrl());
        String resolvedAddress = resolveBookingAddress(request.getAssistedByUserId(), request.getBookingAddress());

        ServiceCase serviceCase = ServiceCase.builder()
                .serviceId(request.getServiceId())
                .serviceName(resolvedName)
                .serviceImageUrl(resolvedImage)
                .description(request.getDescription())
                .customerPhone(request.getCustomerPhone())
                .assistedByUserId(request.getAssistedByUserId())
                .attachmentUrl(request.getAttachmentUrl())
                .bookingAddress(resolvedAddress)
                .status(CaseStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        ServiceCase saved = repository.save(serviceCase);
        auditService.log(AuditAction.CASE_CREATED, request.getAssistedByUserId(), saved.getId());
        // Fire ORDER_CREATED notification
        try {
            Map<String, String> vars = new HashMap<>();
            vars.put("serviceName", saved.getServiceName() != null ? saved.getServiceName() : "Service");
            vars.put("orderId", String.valueOf(saved.getId()));
            notificationService.handleEvent("ORDER_CREATED", saved.getAssistedByUserId(), vars);
        } catch (Exception ignored) {}
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
