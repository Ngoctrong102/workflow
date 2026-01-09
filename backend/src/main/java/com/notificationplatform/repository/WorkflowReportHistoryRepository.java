package com.notificationplatform.repository;

import com.notificationplatform.entity.WorkflowReportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkflowReportHistoryRepository extends JpaRepository<WorkflowReportHistory, String> {

    @Query("SELECT wrh FROM WorkflowReportHistory wrh WHERE wrh.workflow.id = :workflowId ORDER BY wrh.generatedAt DESC")
    List<WorkflowReportHistory> findByWorkflowId(@Param("workflowId") String workflowId);

    @Query("SELECT wrh FROM WorkflowReportHistory wrh WHERE wrh.workflow.id = :workflowId ORDER BY wrh.generatedAt DESC")
    Page<WorkflowReportHistory> findByWorkflowId(@Param("workflowId") String workflowId, Pageable pageable);

    @Query("SELECT wrh FROM WorkflowReportHistory wrh WHERE wrh.workflowReport.id = :workflowReportId ORDER BY wrh.generatedAt DESC")
    List<WorkflowReportHistory> findByWorkflowReportId(@Param("workflowReportId") String workflowReportId);

    @Query("SELECT wrh FROM WorkflowReportHistory wrh WHERE wrh.workflow.id = :workflowId AND " +
           "wrh.reportPeriodStart >= :startDate AND wrh.reportPeriodEnd <= :endDate ORDER BY wrh.generatedAt DESC")
    List<WorkflowReportHistory> findByWorkflowIdAndDateRange(@Param("workflowId") String workflowId,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wrh FROM WorkflowReportHistory wrh WHERE wrh.workflow.id = :workflowId AND " +
           "wrh.reportPeriodStart >= :startDate AND wrh.reportPeriodEnd <= :endDate ORDER BY wrh.generatedAt DESC")
    Page<WorkflowReportHistory> findByWorkflowIdAndDateRange(@Param("workflowId") String workflowId,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              Pageable pageable);

    // Find by generated at between
    @Query("SELECT wrh FROM WorkflowReportHistory wrh WHERE wrh.generatedAt BETWEEN :startDate AND :endDate ORDER BY wrh.generatedAt DESC")
    List<WorkflowReportHistory> findByGeneratedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}

