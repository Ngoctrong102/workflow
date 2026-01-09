package com.notificationplatform.repository;

import com.notificationplatform.entity.NodeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NodeExecutionRepository extends JpaRepository<NodeExecution, String> {

    // Find by execution ID
    List<NodeExecution> findByExecutionId(String executionId);

    // Find by execution ID ordered by started date
    @Query("SELECT ne FROM NodeExecution ne WHERE ne.execution.id = :executionId ORDER BY ne.startedAt ASC")
    List<NodeExecution> findByExecutionIdOrderByStartedAtAsc(@Param("executionId") String executionId);

    @Query("SELECT ne FROM NodeExecution ne WHERE ne.execution.id = :executionId AND ne.nodeId = :nodeId")
    List<NodeExecution> findByExecutionIdAndNodeId(@Param("executionId") String executionId, @Param("nodeId") String nodeId);

    // Find by execution ID and status
    @Query("SELECT ne FROM NodeExecution ne WHERE ne.execution.id = :executionId AND ne.status = :status")
    List<NodeExecution> findByExecutionIdAndStatus(@Param("executionId") String executionId, 
                                                   @Param("status") String status);

    // Find by node ID
    @Query("SELECT ne FROM NodeExecution ne WHERE ne.nodeId = :nodeId")
    List<NodeExecution> findByNodeId(@Param("nodeId") String nodeId);

    // Find failed node executions by execution ID
    @Query("SELECT ne FROM NodeExecution ne WHERE ne.execution.id = :executionId AND ne.status = 'failed'")
    List<NodeExecution> findFailedByExecutionId(@Param("executionId") String executionId);

    // Count by execution ID
    long countByExecutionId(String executionId);

    // Count by execution ID and status
    @Query("SELECT COUNT(ne) FROM NodeExecution ne WHERE ne.execution.id = :executionId AND ne.status = :status")
    long countByExecutionIdAndStatus(@Param("executionId") String executionId, 
                                    @Param("status") String status);

    // Dashboard queries - Node performance
    // Get node performance metrics by workflow
    @Query(value = "SELECT ne.node_id, COUNT(ne.id), " +
           "SUM(CASE WHEN ne.status = 'completed' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN ne.status = 'failed' THEN 1 ELSE 0 END), " +
           "AVG(ne.duration) " +
           "FROM node_executions ne " +
           "JOIN executions e ON e.id = ne.execution_id " +
           "WHERE e.workflow_id = :workflowId AND " +
           "ne.started_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "ne.started_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp)) " +
           "GROUP BY ne.node_id", nativeQuery = true)
    List<Object[]> getNodePerformanceMetrics(@Param("workflowId") String workflowId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // Get node errors by type
    @Query(value = "SELECT ne.node_id, ne.error, COUNT(ne.id) FROM node_executions ne " +
           "JOIN executions e ON e.id = ne.execution_id " +
           "WHERE e.workflow_id = :workflowId AND ne.status = 'failed' AND " +
           "ne.started_at >= COALESCE(CAST(:startDate AS timestamp), CAST('1970-01-01' AS timestamp)) AND " +
           "ne.started_at <= COALESCE(CAST(:endDate AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp)) " +
           "GROUP BY ne.node_id, ne.error", nativeQuery = true)
    List<Object[]> getNodeErrorsByType(@Param("workflowId") String workflowId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Find by node type
    @Query("SELECT ne FROM NodeExecution ne WHERE ne.nodeType = :nodeType")
    List<NodeExecution> findByNodeType(@Param("nodeType") String nodeType);

    // JSONB queries - Find by input data field
    @Query(value = "SELECT * FROM node_executions WHERE input_data->>:fieldName = :fieldValue", nativeQuery = true)
    List<NodeExecution> findByInputDataField(@Param("fieldName") String fieldName, 
                                             @Param("fieldValue") String fieldValue);

    // JSONB queries - Find by output data field
    @Query(value = "SELECT * FROM node_executions WHERE output_data->>:fieldName = :fieldValue", nativeQuery = true)
    List<NodeExecution> findByOutputDataField(@Param("fieldName") String fieldName, 
                                              @Param("fieldValue") String fieldValue);

    // JSONB queries - Find by nested input data field (using path)
    @Query(value = "SELECT * FROM node_executions WHERE input_data#>>:path = :fieldValue", nativeQuery = true)
    List<NodeExecution> findByInputDataPath(@Param("path") String[] path, 
                                           @Param("fieldValue") String fieldValue);

    // JSONB queries - Find by nested output data field (using path)
    @Query(value = "SELECT * FROM node_executions WHERE output_data#>>:path = :fieldValue", nativeQuery = true)
    List<NodeExecution> findByOutputDataPath(@Param("path") String[] path, 
                                            @Param("fieldValue") String fieldValue);
}

