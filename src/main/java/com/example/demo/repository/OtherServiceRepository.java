package com.example.demo.repository;

import com.example.demo.model.OtherService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherServiceRepository extends JpaRepository<OtherService, Long> {
    List<OtherService> findByActiveTrue();
}
