package com.example.demo.repository;
import com.example.demo.model.NotificationCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificationCampaignRepository extends JpaRepository<NotificationCampaign, Long> {
    List<NotificationCampaign> findAllByOrderByCreatedAtDesc();
    List<NotificationCampaign> findByStatus(String status);
}
