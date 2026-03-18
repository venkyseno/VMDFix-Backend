package com.example.demo.repository;
import com.example.demo.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByUserId(Long userId);
    List<NotificationLog> findByCampaignId(Long campaignId);
    long countByStatus(String status);
}
