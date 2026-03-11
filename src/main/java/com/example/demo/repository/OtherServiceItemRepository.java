package com.example.demo.repository;

import com.example.demo.model.OtherServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherServiceItemRepository extends JpaRepository<OtherServiceItem, Long> {
    List<OtherServiceItem> findByOtherServiceId(Long otherServiceId);
}
