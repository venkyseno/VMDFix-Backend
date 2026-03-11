package com.example.demo.repository;

import com.example.demo.model.QuickService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuickServiceRepository extends JpaRepository<QuickService, Long> {
    List<QuickService> findByActiveTrueOrderBySortOrderAsc();
}
