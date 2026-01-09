package com.notificationplatform.repository;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TriggerRepository extends JpaRepository<Trigger, String> {

    // Find active triggers (not soft deleted)
    @Query("SELECT t FROM Trigger t WHERE t.deletedAt IS NULL")
    List<Trigger> findAllActive();

    // Find by workflow ID (excluding soft deleted)
    @Query("SELECT t FROM Trigger t WHERE t.workflow.id = :workflowId AND t.deletedAt IS NULL")
    List<Trigger> findByWorkflowId(@Param("workflowId") String workflowId);

    // Find by trigger type (excluding soft deleted)
    @Query("SELECT t FROM Trigger t WHERE t.triggerType = :triggerType AND t.deletedAt IS NULL")
    List<Trigger> findByTriggerType(@Param("triggerType") TriggerType triggerType);

    // Find by workflow ID and node ID (excluding soft deleted)
    @Query("SELECT t FROM Trigger t WHERE t.workflow.id = :workflowId AND t.nodeId = :nodeId AND t.deletedAt IS NULL")
    Optional<Trigger> findByWorkflowIdAndNodeId(@Param("workflowId") String workflowId, 
                                                @Param("nodeId") String nodeId);

    // Find by type (excluding soft deleted) - deprecated, use findByTriggerType
    @Deprecated
    @Query("SELECT t FROM Trigger t WHERE t.triggerType = :type AND t.deletedAt IS NULL")
    List<Trigger> findByType(@Param("type") TriggerType type);

    // Find by status (excluding soft deleted)
    @Query("SELECT t FROM Trigger t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<Trigger> findByStatus(@Param("status") com.notificationplatform.entity.enums.TriggerStatus status);

    // Find active triggers by workflow ID
    @Query("SELECT t FROM Trigger t WHERE t.workflow.id = :workflowId AND t.status = :status AND t.deletedAt IS NULL")
    List<Trigger> findActiveByWorkflowId(@Param("workflowId") String workflowId, 
                                         @Param("status") com.notificationplatform.entity.enums.TriggerStatus status);
    
    // Find active triggers by workflow ID (convenience method with default ACTIVE status)
    default List<Trigger> findActiveByWorkflowId(String workflowId) {
        return findActiveByWorkflowId(workflowId, com.notificationplatform.entity.enums.TriggerStatus.ACTIVE);
    }

    // Find by workflow ID and type (deprecated, use findByWorkflowIdAndTriggerType)
    @Deprecated
    @Query("SELECT t FROM Trigger t WHERE t.workflow.id = :workflowId AND t.triggerType = :type AND t.deletedAt IS NULL")
    List<Trigger> findByWorkflowIdAndType(@Param("workflowId") String workflowId, 
                                          @Param("type") TriggerType type);

    // Find by workflow ID and trigger type
    @Query("SELECT t FROM Trigger t WHERE t.workflow.id = :workflowId AND t.triggerType = :triggerType AND t.deletedAt IS NULL")
    List<Trigger> findByWorkflowIdAndTriggerType(@Param("workflowId") String workflowId, 
                                                 @Param("triggerType") TriggerType triggerType);

    // Find by workflow ID, trigger type and active status
    @Query("SELECT t FROM Trigger t WHERE t.workflow.id = :workflowId AND t.triggerType = :triggerType AND t.status = 'active' AND t.deletedAt IS NULL")
    List<Trigger> findByWorkflowIdAndTriggerTypeAndActive(@Param("workflowId") String workflowId, 
                                                           @Param("triggerType") TriggerType triggerType);

    // Find by ID and not soft deleted
    @Query("SELECT t FROM Trigger t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Trigger> findByIdAndNotDeleted(@Param("id") String id);

    // Find by created date range
    @Query("SELECT t FROM Trigger t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL")
    List<Trigger> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    // Count triggers by workflow ID
    @Query("SELECT COUNT(t) FROM Trigger t WHERE t.workflow.id = :workflowId AND t.deletedAt IS NULL")
    long countByWorkflowId(@Param("workflowId") String workflowId);

    // Check if trigger exists and is not deleted
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Trigger t WHERE t.id = :id AND t.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") String id);

    // Find by workflow ID and not deleted (alias for findByWorkflowId)
    @Query("SELECT t FROM Trigger t WHERE t.workflow.id = :workflowId AND t.deletedAt IS NULL")
    List<Trigger> findByWorkflowIdAndNotDeleted(@Param("workflowId") String workflowId);

    // Find by path, method and active status
    @Query(value = "SELECT * FROM triggers WHERE config::text LIKE CONCAT('%\"path\":\"', :path, '\"%') AND config::text LIKE CONCAT('%\"method\":\"', :method, '\"%') AND status = 'active' AND deleted_at IS NULL", nativeQuery = true)
    List<Trigger> findByPathAndMethodAndActive(@Param("path") String path, @Param("method") String method);

    // Find by trigger type and active status
    @Query("SELECT t FROM Trigger t WHERE t.triggerType = :triggerType AND t.status = 'active' AND t.deletedAt IS NULL")
    List<Trigger> findByTriggerTypeAndActive(@Param("triggerType") TriggerType triggerType);

    // Find by trigger type and status
    @Query("SELECT t FROM Trigger t WHERE t.triggerType = :triggerType AND t.status = :status AND t.deletedAt IS NULL")
    List<Trigger> findByTriggerTypeAndStatus(@Param("triggerType") TriggerType triggerType, 
                                             @Param("status") com.notificationplatform.entity.enums.TriggerStatus status);

    // Find workflow ID by trigger ID (native query to avoid lazy loading)
    @Query(value = "SELECT workflow_id FROM triggers WHERE id = :triggerId AND deleted_at IS NULL", nativeQuery = true)
    String findWorkflowIdByTriggerId(@Param("triggerId") String triggerId);

    // Find trigger by ID with workflow eagerly loaded (to avoid LazyInitializationException)
    @Query("SELECT t FROM Trigger t JOIN FETCH t.workflow WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Trigger> findByIdWithWorkflow(@Param("id") String id);
}

