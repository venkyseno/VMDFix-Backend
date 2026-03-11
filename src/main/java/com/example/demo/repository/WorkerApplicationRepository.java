package com.example.demo.repository;

import com.example.demo.model.WorkerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerApplicationRepository extends JpaRepository<WorkerApplication, Long> {
    List<WorkerApplication> findByStatus(String status);
}
