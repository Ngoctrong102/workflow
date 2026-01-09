package com.notificationplatform.repository;

import com.notificationplatform.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    @Query("SELECT a FROM Alert a WHERE a.status = :status")
    List<Alert> findByStatus(@Param("status") String status);

    @Query("SELECT a FROM Alert a WHERE a.severity = :severity")
    List<Alert> findBySeverity(@Param("severity") String severity);

    @Query("SELECT a FROM Alert a WHERE a.channelId = :channelId")
    List<Alert> findByChannelId(@Param("channelId") String channelId);

    @Query("SELECT a FROM Alert a WHERE a.alertRuleId = :alertRuleId")
    List<Alert> findByAlertRuleId(@Param("alertRuleId") String alertRuleId);

    @Query("SELECT a FROM Alert a WHERE a.status = 'open'")
    List<Alert> findOpenAlerts();

    @Query("SELECT a FROM Alert a WHERE a.triggeredAt >= :since")
    List<Alert> findAlertsSince(@Param("since") LocalDateTime since);
}

