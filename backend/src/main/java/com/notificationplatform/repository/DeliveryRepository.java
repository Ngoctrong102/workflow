package com.notificationplatform.repository;

import com.notificationplatform.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {

    // Find by notification ID
    List<Delivery> findByNotificationId(String notificationId);

    // Find by recipient
    List<Delivery> findByRecipient(String recipient);

    // Find by channel
    List<Delivery> findByChannel(String channel);

    // Find by status
    List<Delivery> findByStatus(String status);

    // Find by channel and status
    @Query("SELECT d FROM Delivery d WHERE d.channel = :channel AND d.status = :status")
    List<Delivery> findByChannelAndStatus(@Param("channel") String channel, 
                                         @Param("status") String status);

    // Find by notification ID and status
    @Query("SELECT d FROM Delivery d WHERE d.notification.id = :notificationId AND d.status = :status")
    List<Delivery> findByNotificationIdAndStatus(@Param("notificationId") String notificationId, 
                                                 @Param("status") String status);

    // Find by created date range
    @Query("SELECT d FROM Delivery d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<Delivery> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Find by delivered date range
    @Query("SELECT d FROM Delivery d WHERE d.deliveredAt BETWEEN :startDate AND :endDate")
    List<Delivery> findByDeliveredAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    // Count by notification ID
    long countByNotificationId(String notificationId);

    // Count by notification ID and status
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.notification.id = :notificationId AND d.status = :status")
    long countByNotificationIdAndStatus(@Param("notificationId") String notificationId, 
                                       @Param("status") String status);

    // Count by channel and status
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.channel = :channel AND d.status = :status")
    long countByChannelAndStatus(@Param("channel") String channel, 
                                @Param("status") String status);

    // Count delivered by channel
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.channel = :channel AND d.status = 'delivered'")
    long countDeliveredByChannel(@Param("channel") String channel);

    // Count failed by channel
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.channel = :channel AND d.status = 'failed'")
    long countFailedByChannel(@Param("channel") String channel);

    // Find by date range (for analytics)
    @Query("SELECT d FROM Delivery d WHERE " +
           "d.createdAt >= COALESCE(:startDate, '1970-01-01T00:00:00') AND " +
           "d.createdAt <= COALESCE(:endDate, '9999-12-31T23:59:59')")
    List<Delivery> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // Find by channel and date range (for analytics)
    @Query("SELECT d FROM Delivery d WHERE d.channel = :channel AND " +
           "d.createdAt >= COALESCE(:startDate, '1970-01-01T00:00:00') AND " +
           "d.createdAt <= COALESCE(:endDate, '9999-12-31T23:59:59')")
    List<Delivery> findByChannelAndDateRange(@Param("channel") String channel,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // Dashboard queries - Channel performance
    // Get delivery metrics by workflow and channel
    @Query(value = "SELECT d.channel, COUNT(d.id), " +
           "SUM(CASE WHEN d.status = 'delivered' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN d.status = 'failed' THEN 1 ELSE 0 END) " +
           "FROM deliveries d " +
           "JOIN notifications n ON n.id = d.notification_id " +
           "WHERE n.workflow_id = :workflowId AND " +
           "d.created_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "d.created_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp)) " +
           "GROUP BY d.channel", nativeQuery = true)
    List<Object[]> getDeliveryMetricsByChannel(@Param("workflowId") String workflowId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Get deliveries for average delivery time calculation
    // Note: Service layer ensures startDate and endDate are never null
    @Query("SELECT d FROM Delivery d WHERE d.notification.workflow.id = :workflowId AND " +
           "d.createdAt >= :startDate AND " +
           "d.createdAt <= :endDate AND " +
           "d.deliveredAt IS NOT NULL AND d.createdAt IS NOT NULL")
    List<com.notificationplatform.entity.Delivery> getDeliveriesForAverageTime(@Param("workflowId") String workflowId,
                                                                               @Param("startDate") LocalDateTime startDate,
                                                                               @Param("endDate") LocalDateTime endDate);

    // Count delivered notifications by workflow
    @Query(value = "SELECT COUNT(d.id) FROM deliveries d " +
           "JOIN notifications n ON n.id = d.notification_id " +
           "WHERE n.workflow_id = :workflowId AND " +
           "d.status = 'delivered' AND " +
           "d.created_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "d.created_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp))", nativeQuery = true)
    long countDeliveredByWorkflowId(@Param("workflowId") String workflowId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}

