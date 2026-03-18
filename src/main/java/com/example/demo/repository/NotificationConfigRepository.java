package com.example.demo.repository;
import com.example.demo.model.NotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, Long> {
    List<NotificationConfig> findByEventType(String eventType);
    Optional<NotificationConfig> findByEventTypeAndTargetRole(String eventType, String targetRole);
}
