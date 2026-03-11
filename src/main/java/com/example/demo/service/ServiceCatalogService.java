package com.example.demo.service;

import com.example.demo.exception.InvalidServiceException;
import com.example.demo.model.ServiceItem;
import com.example.demo.repository.ServiceItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCatalogService {

    private final ServiceItemRepository repository;

    public ServiceCatalogService(ServiceItemRepository repository) {
        this.repository = repository;
    }

    public List<ServiceItem> getActiveServices() {
        return repository.findByActiveTrue();
    }

    public ServiceItem getActiveServiceById(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new InvalidServiceException("Invalid or inactive service ID: " + id)
                );
    }
}
