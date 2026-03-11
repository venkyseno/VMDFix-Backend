package com.example.demo.repository;

import com.example.demo.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByActiveTrue();

    Optional<ServiceItem> findByIdAndActiveTrue(Long id);
}
