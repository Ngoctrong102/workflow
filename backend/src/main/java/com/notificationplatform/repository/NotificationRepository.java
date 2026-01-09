package com.notificationplatform.repository;

import com.notificationplatform.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // Find by execution ID
    List<Notification> findByExecutionId(String executionId);

    // Find by workflow ID
    List<Notification> findByWorkflowId(String workflowId);

    // Find by channel
    List<Notification> findByChannel(String channel);

    // Find by status
    List<Notification> findByStatus(String status);

    // Find by execution ID and status
    @Query("SELECT n FROM Notification n WHERE n.execution.id = :executionId AND n.status = :status")
    List<Notification> findByExecutionIdAndStatus(@Param("executionId") String executionId, 
                                                  @Param("status") String status);

    // Find by workflow ID and channel
    @Query("SELECT n FROM Notification n WHERE n.workflow.id = :workflowId AND n.channel = :channel")
    List<Notification> findByWorkflowIdAndChannel(@Param("workflowId") String workflowId, 
                                                  @Param("channel") String channel);

    // Find by created date range
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    // Find by workflow ID ordered by created date
    @Query("SELECT n FROM Notification n WHERE n.workflow.id = :workflowId ORDER BY n.createdAt DESC")
    List<Notification> findByWorkflowIdOrderByCreatedAtDesc(@Param("workflowId") String workflowId);

    // Count by workflow ID
    long countByWorkflowId(String workflowId);

    // Count by workflow ID and status
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.workflow.id = :workflowId AND n.status = :status")
    long countByWorkflowIdAndStatus(@Param("workflowId") String workflowId, 
                                    @Param("status") String status);

    // Count by channel
    long countByChannel(String channel);

    // Dashboard queries - Channel performance
    // Get notifications by workflow and channel
    @Query(value = "SELECT n.channel, COUNT(n.id), " +
           "SUM(CASE WHEN n.status = 'sent' THEN 1 ELSE 0 END) " +
           "FROM notifications n WHERE n.workflow_id = :workflowId AND " +
           "n.created_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "n.created_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp)) " +
           "GROUP BY n.channel", nativeQuery = true)
    List<Object[]> getNotificationsByChannel(@Param("workflowId") String workflowId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Count notifications by workflow
    @Query(value = "SELECT COUNT(n.id) FROM notifications n WHERE n.workflow_id = :workflowId AND " +
           "n.created_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "n.created_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp))", nativeQuery = true)
    long countByWorkflowIdAndDateRange(@Param("workflowId") String workflowId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}

