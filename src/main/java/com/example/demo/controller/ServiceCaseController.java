package com.example.demo.controller;

import com.example.demo.api.SuccessResponse;
import com.example.demo.dto.CloseCaseRequest;
import com.example.demo.dto.CreateCaseRequest;
import com.example.demo.model.ServiceCase;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ServiceCaseService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin
@RequiredArgsConstructor
public class ServiceCaseController {

    private final ServiceCaseService service;
    private final UserRepository userRepository;

    /** DTO that enriches ServiceCase with worker info for the Orders page */
    @Getter
    @Setter
    public static class EnrichedCase {
        private Long id;
        private Long serviceId;
        private String serviceName;
        private String serviceImageUrl;
        private String description;
        private String customerPhone;
        private Long assistedByUserId;
        private Long workerId;
        private String workerName;
        private String workerPhone;
        private String attachmentUrl;
        private String status;
        private BigDecimal serviceAmount;
        private LocalDateTime createdAt;
        private String bookingAddress;

        public static EnrichedCase from(ServiceCase sc, User worker) {
            EnrichedCase e = new EnrichedCase();
            e.id = sc.getId();
            e.serviceId = sc.getServiceId();
            e.serviceName = sc.getServiceName();
            e.serviceImageUrl = sc.getServiceImageUrl();
            e.description = sc.getDescription();
            e.customerPhone = sc.getCustomerPhone();
            e.assistedByUserId = sc.getAssistedByUserId();
            e.workerId = sc.getWorkerId();
            e.attachmentUrl = sc.getAttachmentUrl();
            e.status = sc.getStatus() != null ? sc.getStatus().name() : "CREATED";
            e.serviceAmount = sc.getServiceAmount();
            e.createdAt = sc.getCreatedAt();
            e.bookingAddress = sc.getBookingAddress();
            if (worker != null) {
                e.workerName = worker.getName();
                e.workerPhone = worker.getMobile();
            }
            return e;
        }
    }

    @PostMapping
    public SuccessResponse<ServiceCase> createCase(@RequestBody CreateCaseRequest request) {
        return SuccessResponse.ok(service.createCase(request));
    }

    @GetMapping
    public SuccessResponse<List<ServiceCase>> getAllCases() {
        return SuccessResponse.ok(service.getAllCases());
    }

    @GetMapping("/{id}")
    public SuccessResponse<EnrichedCase> getCase(@PathVariable Long id) {
        ServiceCase sc = service.getCaseById(id);
        User worker = sc.getWorkerId() != null ? userRepository.findById(sc.getWorkerId()).orElse(null) : null;
        return SuccessResponse.ok(EnrichedCase.from(sc, worker));
    }

    @GetMapping("/user/{userId}")
    public SuccessResponse<List<EnrichedCase>> getCasesByUser(@PathVariable Long userId) {
        List<ServiceCase> cases = service.getCasesByUser(userId);
        List<EnrichedCase> enriched = cases.stream().map(sc -> {
            User worker = sc.getWorkerId() != null ? userRepository.findById(sc.getWorkerId()).orElse(null) : null;
            return EnrichedCase.from(sc, worker);
        }).collect(Collectors.toList());
        return SuccessResponse.ok(enriched);
    }

    @PostMapping("/{id}/close")
    public SuccessResponse<ServiceCase> closeCase(
            @PathVariable Long id,
            @RequestBody CloseCaseRequest request,
            @RequestHeader(name = "X-USER-ID", required = false) Long userId
    ) {
        return SuccessResponse.ok(service.closeCase(id, request, userId));
    }

    /**
     * Admin: Fix serviceName for a specific order.
     * POST /api/cases/{id}/fix-name?name=HouseClean
     */
    @PostMapping("/{id}/fix-name")
    public SuccessResponse<ServiceCase> fixServiceName(
            @PathVariable Long id,
            @RequestParam String name) {
        return SuccessResponse.ok(service.updateServiceName(id, name));
    }

    /**
     * Admin: Bulk-fix all orders with missing/wrong serviceName.
     * POST /api/cases/bulk-fix-names
     * Returns count of records fixed.
     */
    @PostMapping("/bulk-fix-names")
    public SuccessResponse<Map<String, Object>> bulkFixNames() {
        int fixed = service.bulkFixServiceNames();
        return SuccessResponse.ok(Map.of("fixed", fixed, "message", "Fixed " + fixed + " records"));
    }

}