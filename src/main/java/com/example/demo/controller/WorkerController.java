package com.example.demo.controller;

import com.example.demo.api.SuccessResponse;
import com.example.demo.model.ServiceCase;
import com.example.demo.service.ServiceCaseService;
import com.example.demo.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;
    private final ServiceCaseService serviceCaseService;

    // GET /api/worker/cases/{workerId}
    @GetMapping("/api/worker/cases/{workerId}")
    public SuccessResponse<List<ServiceCase>> getWorkerCases(@PathVariable Long workerId) {
        return SuccessResponse.ok(serviceCaseService.getCasesByWorker(workerId));
    }

    // POST /api/cases/{id}/assign-worker?workerId=
    @PostMapping("/api/cases/{id}/assign-worker")
    public SuccessResponse<ServiceCase> assignWorker(
            @PathVariable Long id,
            @RequestParam Long workerId
    ) {
        return SuccessResponse.ok(workerService.assignWorker(id, workerId));
    }

    // POST /api/cases/{id}/start-work?workerId=
    @PostMapping("/api/cases/{id}/start-work")
    public SuccessResponse<ServiceCase> startWork(
            @PathVariable Long id,
            @RequestParam Long workerId
    ) {
        return SuccessResponse.ok(workerService.startWork(id, workerId));
    }

    // POST /api/cases/{id}/complete-work?workerId=
    @PostMapping("/api/cases/{id}/complete-work")
    public SuccessResponse<ServiceCase> completeWork(
            @PathVariable Long id,
            @RequestParam Long workerId
    ) {
        return SuccessResponse.ok(workerService.markWorkCompleted(id, workerId));
    }
}
