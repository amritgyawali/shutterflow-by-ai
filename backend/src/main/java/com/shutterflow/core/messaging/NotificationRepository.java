package com.shutterflow.core.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);
    List<Notification> findByClientIdAndIsReadFalseOrderByCreatedAtDesc(String clientId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
}
