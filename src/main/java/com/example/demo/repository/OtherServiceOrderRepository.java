package com.example.demo.repository;

import com.example.demo.model.OtherServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherServiceOrderRepository extends JpaRepository<OtherServiceOrder, Long> {
    List<OtherServiceOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
