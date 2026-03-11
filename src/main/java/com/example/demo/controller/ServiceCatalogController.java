package com.example.demo.controller;

import com.example.demo.model.ServiceItem;
import com.example.demo.service.ServiceCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin
public class ServiceCatalogController {

    private final ServiceCatalogService service;

    public ServiceCatalogController(ServiceCatalogService service) {
        this.service = service;
    }

    /**
     * Home screen services (cards)
     */
    @GetMapping
    public List<ServiceItem> getActiveServices() {
        return service.getActiveServices();
    }
}
