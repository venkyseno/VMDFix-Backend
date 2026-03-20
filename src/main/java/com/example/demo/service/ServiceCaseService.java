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
     * Resolve service name.
     * Priority: providedName (frontend always knows what it showed) → DB lookup → fallback.
     * This prevents ID collisions between our_service_cards and quick_services tables.
     */
    private String resolveServiceName(Long serviceId, String providedName, String serviceType) {
        // Frontend explicitly told us the name — trust it. This is the most reliable source.
        if (providedName != null && !providedName.isBlank()) return providedName;

        if (serviceId != null) {
            // If serviceType is known, look in the correct table only
            if ("our".equalsIgnoreCase(serviceType)) {
                var card = ourServiceCardRepository.findById(serviceId);
                if (card.isPresent()) return card.get().getName();
            } else if ("quick".equalsIgnoreCase(serviceType)) {
                var qs = quickServiceRepository.findById(serviceId);
                if (qs.isPresent()) return qs.get().getName();
            } else {
                // Legacy fallback: try both (may collide, but providedName already handled above)
                var card = ourServiceCardRepository.findById(serviceId);
                if (card.isPresent()) return card.get().getName();
                var qs = quickServiceRepository.findById(serviceId);
                if (qs.isPresent()) return qs.get().getName();
            }
        }
        return serviceId != null ? "Service #" + serviceId : "Service";
    }

    private String resolveServiceImage(Long serviceId, String providedImage, String serviceType) {
        // Frontend provided image takes priority
        if (providedImage != null && !providedImage.isBlank()) return providedImage;

        if (serviceId != null) {
            if ("our".equalsIgnoreCase(serviceType)) {
                var card = ourServiceCardRepository.findById(serviceId);
                if (card.isPresent() && card.get().getImageUrl() != null) return card.get().getImageUrl();
            } else if ("quick".equalsIgnoreCase(serviceType)) {
                var qs = quickServiceRepository.findById(serviceId);
                if (qs.isPresent() && qs.get().getImageUrl() != null) return qs.get().getImageUrl();
            } else {
                var card = ourServiceCardRepository.findById(serviceId);
                if (card.isPresent() && card.get().getImageUrl() != null) return card.get().getImageUrl();
                var qs = quickServiceRepository.findById(serviceId);
                if (qs.isPresent() && qs.get().getImageUrl() != null) return qs.get().getImageUrl();
            }
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
        String resolvedName = resolveServiceName(request.getServiceId(), request.getServiceName(), request.getServiceType());
        String resolvedImage = resolveServiceImage(request.getServiceId(), request.getServiceImageUrl(), request.getServiceType());
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

    /**
     * Fix serviceName for a specific case (admin use).
     * Corrects old records where serviceName was stored from the wrong table.
     */
    public ServiceCase updateServiceName(Long caseId, String newServiceName) {
        ServiceCase sc = getCaseById(caseId);
        if (newServiceName != null && !newServiceName.isBlank()) {
            sc.setServiceName(newServiceName.trim());
            repository.save(sc);
        }
        return sc;
    }

    /**
     * Bulk-fix all cases where serviceName is missing or generic ("Service #N").
     * Re-resolves from the correct service table using serviceId.
     * Called once via admin endpoint to clean up old data.
     */
    public int bulkFixServiceNames() {
        int fixed = 0;
        for (ServiceCase sc : repository.findAll()) {
            String name = sc.getServiceName();
            boolean needsFix = name == null || name.isBlank() || name.startsWith("Service #");
            if (needsFix && sc.getServiceId() != null) {
                var card = ourServiceCardRepository.findById(sc.getServiceId());
                if (card.isPresent()) {
                    sc.setServiceName(card.get().getName());
                    repository.save(sc);
                    fixed++;
                    continue;
                }
                var qs = quickServiceRepository.findById(sc.getServiceId());
                if (qs.isPresent()) {
                    sc.setServiceName(qs.get().getName());
                    repository.save(sc);
                    fixed++;
                }
            }
        }
        return fixed;
    }

}