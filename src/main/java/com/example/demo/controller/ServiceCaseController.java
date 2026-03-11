package com.example.demo.controller;

import com.example.demo.api.SuccessResponse;
import com.example.demo.dto.CloseCaseRequest;
import com.example.demo.dto.CreateCaseRequest;
import com.example.demo.model.ServiceCase;
import com.example.demo.service.ServiceCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin
@RequiredArgsConstructor
public class ServiceCaseController {

    private final ServiceCaseService service;

    @PostMapping
    public SuccessResponse<ServiceCase> createCase(@RequestBody CreateCaseRequest request) {
        return SuccessResponse.ok(service.createCase(request));
    }

    @GetMapping
    public SuccessResponse<List<ServiceCase>> getAllCases() {
        return SuccessResponse.ok(service.getAllCases());
    }

    @GetMapping("/{id}")
    public SuccessResponse<ServiceCase> getCase(@PathVariable Long id) {
        return SuccessResponse.ok(service.getCaseById(id));
    }

    @GetMapping("/user/{userId}")
    public SuccessResponse<List<ServiceCase>> getCasesByUser(@PathVariable Long userId) {
        return SuccessResponse.ok(service.getCasesByUser(userId));
    }

    @PostMapping("/{id}/close")
    public SuccessResponse<ServiceCase> closeCase(
            @PathVariable Long id,
            @RequestBody CloseCaseRequest request,
            @RequestHeader(name = "X-USER-ID", required = false) Long userId
    ) {
        return SuccessResponse.ok(service.closeCase(id, request, userId));
    }
}
