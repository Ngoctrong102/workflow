package com.notificationplatform.repository;

import com.notificationplatform.entity.AnalyticsDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsDaily, String> {

    // Find by date
    List<AnalyticsDaily> findByDate(LocalDate date);

    // Find by workflow ID
    List<AnalyticsDaily> findByWorkflowId(String workflowId);

    // Find by channel
    List<AnalyticsDaily> findByChannel(String channel);

    // Find by metric type
    List<AnalyticsDaily> findByMetricType(String metricType);

    // Find by date range
    @Query("SELECT a FROM AnalyticsDaily a WHERE a.date BETWEEN :startDate AND :endDate")
    List<AnalyticsDaily> findByDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    // Find by workflow ID and date range
    @Query("SELECT a FROM AnalyticsDaily a WHERE a.workflow.id = :workflowId AND a.date BETWEEN :startDate AND :endDate")
    List<AnalyticsDaily> findByWorkflowIdAndDateBetween(@Param("workflowId") String workflowId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // Find by workflow ID, channel and date range
    @Query("SELECT a FROM AnalyticsDaily a WHERE a.workflow.id = :workflowId AND a.channel = :channel AND a.date BETWEEN :startDate AND :endDate")
    List<AnalyticsDaily> findByWorkflowIdAndChannelAndDateBetween(@Param("workflowId") String workflowId,
                                                                  @Param("channel") String channel,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    // Find unique record by date, workflow, channel and metric type
    @Query("SELECT a FROM AnalyticsDaily a WHERE a.date = :date AND " +
           "(:workflowId IS NULL OR a.workflow.id = :workflowId) AND " +
           "(:channel IS NULL OR a.channel = :channel) AND " +
           "a.metricType = :metricType")
    Optional<AnalyticsDaily> findByDateAndWorkflowIdAndChannelAndMetricType(@Param("date") LocalDate date,
                                                                            @Param("workflowId") String workflowId,
                                                                            @Param("channel") String channel,
                                                                            @Param("metricType") String metricType);

    // Sum metric value by workflow ID and date range
    @Query("SELECT SUM(a.metricValue) FROM AnalyticsDaily a WHERE a.workflow.id = :workflowId AND a.metricType = :metricType AND a.date BETWEEN :startDate AND :endDate")
    Long sumMetricValueByWorkflowIdAndMetricTypeAndDateBetween(@Param("workflowId") String workflowId,
                                                               @Param("metricType") String metricType,
                                                               @Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    // Sum metric value by channel and date range
    @Query("SELECT SUM(a.metricValue) FROM AnalyticsDaily a WHERE a.channel = :channel AND a.metricType = :metricType AND a.date BETWEEN :startDate AND :endDate")
    Long sumMetricValueByChannelAndMetricTypeAndDateBetween(@Param("channel") String channel,
                                                            @Param("metricType") String metricType,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    // Count by workflow ID
    long countByWorkflowId(String workflowId);

    // Count by date
    long countByDate(LocalDate date);
}

