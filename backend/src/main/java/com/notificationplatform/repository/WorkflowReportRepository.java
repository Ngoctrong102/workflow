package com.notificationplatform.repository;

import com.notificationplatform.entity.WorkflowReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowReportRepository extends JpaRepository<WorkflowReport, String> {

    @Query("SELECT wr FROM WorkflowReport wr WHERE wr.workflow.id = :workflowId AND wr.deletedAt IS NULL")
    Optional<WorkflowReport> findByWorkflowId(@Param("workflowId") String workflowId);

    @Query("SELECT wr FROM WorkflowReport wr WHERE wr.id = :id AND wr.deletedAt IS NULL")
    Optional<WorkflowReport> findByIdAndNotDeleted(@Param("id") String id);

    @Query("SELECT wr FROM WorkflowReport wr WHERE wr.deletedAt IS NULL")
    List<WorkflowReport> findAllActive();

    @Query("SELECT wr FROM WorkflowReport wr WHERE wr.status = 'active' AND wr.deletedAt IS NULL")
    List<WorkflowReport> findActiveReports();

    @Query("SELECT wr FROM WorkflowReport wr WHERE wr.status = 'active' AND " +
           "(wr.nextGenerationAt IS NULL OR wr.nextGenerationAt <= :now) AND wr.deletedAt IS NULL")
    List<WorkflowReport> findDueReports(@Param("now") LocalDateTime now);

    @Query("SELECT wr FROM WorkflowReport wr WHERE wr.status = :status AND wr.deletedAt IS NULL")
    List<WorkflowReport> findByStatus(@Param("status") String status);

    // Find by next generation at less than or equal and status
    @Query("SELECT wr FROM WorkflowReport wr WHERE wr.nextGenerationAt <= :nextGenerationAt AND wr.status = :status AND wr.deletedAt IS NULL")
    List<WorkflowReport> findByNextGenerationAtLessThanEqualAndStatus(@Param("nextGenerationAt") LocalDateTime nextGenerationAt,
                                                                       @Param("status") String status);
}

