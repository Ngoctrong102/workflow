package com.notificationplatform.repository;

import com.notificationplatform.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, String> {

    // Find by workflow ID
    List<Execution> findByWorkflowId(String workflowId);

    // Find by trigger ID
    List<Execution> findByTriggerId(String triggerId);

    // Find by status
    List<Execution> findByStatus(String status);

    // Find by workflow ID and status
    @Query("SELECT e FROM Execution e WHERE e.workflow.id = :workflowId AND e.status = :status")
    List<Execution> findByWorkflowIdAndStatus(@Param("workflowId") String workflowId, 
                                              @Param("status") String status);

    // Find by workflow ID ordered by started date
    @Query("SELECT e FROM Execution e WHERE e.workflow.id = :workflowId ORDER BY e.startedAt DESC")
    List<Execution> findByWorkflowIdOrderByStartedAtDesc(@Param("workflowId") String workflowId);

    // Find by date range
    @Query("SELECT e FROM Execution e WHERE e.startedAt BETWEEN :startDate AND :endDate")
    List<Execution> findByStartedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    // Find by workflow ID and date range
    @Query("SELECT e FROM Execution e WHERE e.workflow.id = :workflowId AND e.startedAt BETWEEN :startDate AND :endDate ORDER BY e.startedAt DESC")
    List<Execution> findByWorkflowIdAndStartedAtBetween(@Param("workflowId") String workflowId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    // Count by workflow ID
    long countByWorkflowId(String workflowId);

    // Count by workflow ID and status
    @Query("SELECT COUNT(e) FROM Execution e WHERE e.workflow.id = :workflowId AND e.status = :status")
    long countByWorkflowIdAndStatus(@Param("workflowId") String workflowId, 
                                    @Param("status") String status);

    // Find running executions
    @Query("SELECT e FROM Execution e WHERE e.status = 'running'")
    List<Execution> findRunningExecutions();

    // Find failed executions by workflow ID
    @Query("SELECT e FROM Execution e WHERE e.workflow.id = :workflowId AND e.status = 'failed' ORDER BY e.startedAt DESC")
    List<Execution> findFailedByWorkflowId(@Param("workflowId") String workflowId);

    // Find by workflow ID and date range (for analytics)
    @Query("SELECT e FROM Execution e WHERE e.workflow.id = :workflowId AND " +
           "e.startedAt >= COALESCE(:startDate, '1970-01-01T00:00:00') AND " +
           "e.startedAt <= COALESCE(:endDate, '9999-12-31T23:59:59')")
    List<Execution> findByWorkflowIdAndDateRange(@Param("workflowId") String workflowId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Find by date range (for analytics)
    @Query("SELECT e FROM Execution e WHERE " +
           "e.startedAt >= COALESCE(:startDate, '1970-01-01T00:00:00') AND " +
           "e.startedAt <= COALESCE(:endDate, '9999-12-31T23:59:59')")
    List<Execution> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Dashboard queries
    // Count executions by workflow and status
    @Query(value = "SELECT COUNT(*) FROM executions e WHERE e.workflow_id = :workflowId AND e.status = :status AND " +
           "e.started_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "e.started_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp))", nativeQuery = true)
    long countByWorkflowIdAndStatusAndDateRange(@Param("workflowId") String workflowId,
                                                 @Param("status") String status,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // Get average execution time
    @Query(value = "SELECT AVG(e.duration) FROM executions e WHERE e.workflow_id = :workflowId AND " +
           "e.started_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "e.started_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp)) AND e.duration IS NOT NULL", nativeQuery = true)
    Double getAverageExecutionTime(@Param("workflowId") String workflowId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Get executions by trigger type
    @Query(value = "SELECT t.type, COUNT(e.*) FROM executions e " +
           "JOIN triggers t ON e.trigger_id = t.id WHERE e.workflow_id = :workflowId AND " +
           "e.started_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "e.started_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp)) GROUP BY t.type", nativeQuery = true)
    List<Object[]> countExecutionsByTriggerType(@Param("workflowId") String workflowId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Get first and last execution dates
    @Query(value = "SELECT MIN(e.started_at), MAX(e.started_at) FROM executions e WHERE e.workflow_id = :workflowId AND " +
           "e.started_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "e.started_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp))", nativeQuery = true)
    Object[] getFirstAndLastExecutionDates(@Param("workflowId") String workflowId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // JSONB queries - Find by context field
    @Query(value = "SELECT * FROM executions WHERE context->>:fieldName = :fieldValue", nativeQuery = true)
    List<Execution> findByContextField(@Param("fieldName") String fieldName, 
                                       @Param("fieldValue") String fieldValue);

    // JSONB queries - Find by trigger data field
    @Query(value = "SELECT * FROM executions WHERE trigger_data->>:fieldName = :fieldValue", nativeQuery = true)
    List<Execution> findByTriggerDataField(@Param("fieldName") String fieldName, 
                                           @Param("fieldValue") String fieldValue);

    // JSONB queries - Find by nested context field (using path)
    @Query(value = "SELECT * FROM executions WHERE context#>>:path = :fieldValue", nativeQuery = true)
    List<Execution> findByContextPath(@Param("path") String[] path, 
                                      @Param("fieldValue") String fieldValue);

    // JSONB queries - Find by nested trigger data field (using path)
    @Query(value = "SELECT * FROM executions WHERE trigger_data#>>:path = :fieldValue", nativeQuery = true)
    List<Execution> findByTriggerDataPath(@Param("path") String[] path, 
                                         @Param("fieldValue") String fieldValue);
}

