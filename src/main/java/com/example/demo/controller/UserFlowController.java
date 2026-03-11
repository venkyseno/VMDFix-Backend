package com.example.demo.controller;

import com.example.demo.model.Address;
import com.example.demo.model.WorkerApplication;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.WorkerApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-flow")
@CrossOrigin
@RequiredArgsConstructor
public class UserFlowController {

    private static final Logger log = LoggerFactory.getLogger(UserFlowController.class);

    private final AddressRepository addressRepository;
    private final WorkerApplicationRepository workerApplicationRepository;

    @GetMapping("/addresses/{userId}")
    public List<Address> getAddresses(@PathVariable Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @PostMapping("/addresses")
    @Transactional
    public Address addAddress(@RequestBody Address address) {
        if (address.getAddressLine() == null || address.getAddressLine().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address is mandatory");
        }
        if (address.getCity() == null || address.getCity().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City is mandatory");
        }
        // If this is set as primary, clear all others first
        if (Boolean.TRUE.equals(address.getPrimaryAddress())) {
            clearPrimaryForUser(address.getUserId());
        }
        return addressRepository.save(address);
    }

    @PutMapping("/addresses/{id}")
    @Transactional
    public Address updateAddress(@PathVariable Long id, @RequestBody Address payload) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        if (payload.getAddressLine() == null || payload.getAddressLine().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address is mandatory");
        }
        if (payload.getCity() == null || payload.getCity().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City is mandatory");
        }

        // If setting as primary, clear other primaries first
        if (Boolean.TRUE.equals(payload.getPrimaryAddress()) && !Boolean.TRUE.equals(address.getPrimaryAddress())) {
            Long userId = address.getUserId();
            clearPrimaryForUser(userId);
        }

        address.setAddressLine(payload.getAddressLine());
        address.setCity(payload.getCity());
        address.setLandmark(payload.getLandmark());
        address.setPrimaryAddress(payload.getPrimaryAddress());
        return addressRepository.save(address);
    }

    /**
     * PUT /api/user-flow/addresses/{id}/primary
     * Sets the specified address as the primary address for the user.
     * Clears primary flag on all other addresses for that user.
     */
    @PutMapping("/addresses/{id}/primary")
    @Transactional
    public Map<String, Object> setPrimaryAddress(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        log.info("Setting primary address id={}", id);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found: " + id));

        Long userId = address.getUserId();

        // If userId provided in body use it; otherwise use address's userId
        if (body != null && body.containsKey("userId")) {
            Object rawUserId = body.get("userId");
            if (rawUserId instanceof Number) {
                userId = ((Number) rawUserId).longValue();
            }
        }

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot determine userId for address");
        }

        // Step 1: Clear all primaries for this user
        clearPrimaryForUser(userId);

        // Step 2: Set this one as primary
        address.setPrimaryAddress(true);
        Address saved = addressRepository.save(address);

        log.info("Primary address set to id={} for userId={}", id, userId);
        return Map.of(
                "success", true,
                "addressId", saved.getId(),
                "userId", userId,
                "message", "Primary address updated successfully"
        );
    }

    @DeleteMapping("/addresses/{id}")
    @Transactional
    public Map<String, Object> deleteAddress(@PathVariable Long id) {
        if (!addressRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
        }
        addressRepository.deleteById(id);
        return Map.of("success", true, "message", "Address deleted");
    }

    @PostMapping("/worker-apply")
    public WorkerApplication workerApply(@RequestBody WorkerApplication request) {
        request.setStatus("PENDING");
        return workerApplicationRepository.save(request);
    }

    // ---- helpers ----

    private void clearPrimaryForUser(Long userId) {
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        userAddresses.forEach(a -> {
            if (Boolean.TRUE.equals(a.getPrimaryAddress())) {
                a.setPrimaryAddress(false);
                addressRepository.save(a);
            }
        });
    }
}
