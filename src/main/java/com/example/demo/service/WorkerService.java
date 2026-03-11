package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.model.CaseStatus;
import com.example.demo.model.ServiceCase;
import com.example.demo.repository.ServiceCaseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerService.class);
    private final ServiceCaseRepository repository;

    private ServiceCase getCase(Long caseId) {
        return repository.findById(caseId)
                .orElseThrow(() -> new BusinessException("Case not found: " + caseId));
    }

    public ServiceCase assignWorker(Long caseId, Long workerId) {
        ServiceCase sc = getCase(caseId);
        if (sc.getStatus() != CaseStatus.CREATED)
            throw new BusinessException("Worker can only be assigned to CREATED cases");
        sc.setWorkerId(workerId);
        sc.setStatus(CaseStatus.ASSIGNED);
        return repository.save(sc);
    }

    public ServiceCase startWork(Long caseId, Long workerId) {
        ServiceCase sc = getCase(caseId);
        if (sc.getStatus() == CaseStatus.IN_PROGRESS) return sc; // idempotent
        if (sc.getStatus() != CaseStatus.ASSIGNED)
            throw new BusinessException("Work can only start on ASSIGNED cases (current: " + sc.getStatus() + ")");
        if (!workerId.equals(sc.getWorkerId()))
            throw new BusinessException("This worker is not assigned to the case");
        sc.setStatus(CaseStatus.IN_PROGRESS);
        return repository.save(sc);
    }

    public ServiceCase markWorkCompleted(Long caseId, Long workerId) {
        ServiceCase sc = getCase(caseId);
        CaseStatus current = sc.getStatus();
        log.info("markWorkCompleted caseId={} workerId={} status={} assigned={}", caseId, workerId, current, sc.getWorkerId());

        // Idempotent - already done
        if (current == CaseStatus.WORK_DONE || current == CaseStatus.CLOSED) return sc;

        // Auto-assign if missing (edge case)
        if (sc.getWorkerId() == null) sc.setWorkerId(workerId);

        // Accept ASSIGNED or IN_PROGRESS (worker may skip Start Work)
        if (current == CaseStatus.ASSIGNED || current == CaseStatus.IN_PROGRESS) {
            if (!workerId.equals(sc.getWorkerId()))
                throw new BusinessException("Only the assigned worker can complete the work");
            sc.setStatus(CaseStatus.WORK_DONE);
            return repository.save(sc);
        }

        throw new BusinessException("Cannot complete: case " + caseId + " is in status " + current);
    }
}
