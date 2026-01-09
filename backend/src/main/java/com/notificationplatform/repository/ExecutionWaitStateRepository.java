package com.notificationplatform.repository;

import com.notificationplatform.entity.ExecutionWaitState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutionWaitStateRepository extends JpaRepository<ExecutionWaitState, String> {

    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.execution.id = :executionId AND ews.nodeId = :nodeId")
    Optional<ExecutionWaitState> findByExecutionIdAndNodeId(@Param("executionId") String executionId, 
                                                             @Param("nodeId") String nodeId);

    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.execution.id = :executionId AND " +
           "ews.correlationId = :correlationId AND ews.status = :status")
    Optional<ExecutionWaitState> findByExecutionIdAndCorrelationIdAndStatus(
            @Param("executionId") String executionId,
            @Param("correlationId") String correlationId,
            @Param("status") String status);

    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.correlationId = :correlationId AND ews.status = :status")
    List<ExecutionWaitState> findByCorrelationIdAndStatus(@Param("correlationId") String correlationId,
                                                          @Param("status") String status);

    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.status = :status AND ews.expiresAt < :expiresAt")
    List<ExecutionWaitState> findByStatusAndExpiresAtBefore(@Param("status") String status,
                                                            @Param("expiresAt") LocalDateTime expiresAt);

    // Find by correlation ID
    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.correlationId = :correlationId")
    List<ExecutionWaitState> findByCorrelationId(@Param("correlationId") String correlationId);

    // Find by expires at less than or equal and status
    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.expiresAt <= :expiresAt AND ews.status = :status")
    List<ExecutionWaitState> findByExpiresAtLessThanEqualAndStatus(@Param("expiresAt") LocalDateTime expiresAt,
                                                                    @Param("status") String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.id = :id")
    Optional<ExecutionWaitState> findByIdWithLock(@Param("id") String id);

    @Query("SELECT ews FROM ExecutionWaitState ews WHERE ews.execution.id = :executionId")
    List<ExecutionWaitState> findByExecutionId(@Param("executionId") String executionId);

    @Query("DELETE FROM ExecutionWaitState ews WHERE ews.execution.id = :executionId")
    void deleteByExecutionId(@Param("executionId") String executionId);
}

