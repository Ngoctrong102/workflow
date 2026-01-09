package com.notificationplatform.repository;

import com.notificationplatform.entity.RetrySchedule;
import com.notificationplatform.entity.enums.RetryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RetrySchedule entity - Stores retry tasks for failed node executions and executions.
 */
@Repository
public interface RetryScheduleRepository extends JpaRepository<RetrySchedule, String> {

    // Find by status (pending, scheduled)
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.status IN :statuses")
    List<RetrySchedule> findByStatusIn(@Param("statuses") List<RetryStatus> statuses);

    // Find by scheduled time and status (for processing)
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.scheduledAt <= :scheduledAt AND rs.status = :status")
    List<RetrySchedule> findByScheduledAtLessThanEqualAndStatus(@Param("scheduledAt") LocalDateTime scheduledAt,
                                                                 @Param("status") RetryStatus status);

    // Find by execution ID
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.execution.id = :executionId")
    List<RetrySchedule> findByExecutionId(@Param("executionId") String executionId);

    // Find by target ID
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.targetId = :targetId")
    List<RetrySchedule> findByTargetId(@Param("targetId") String targetId);

    // Find by retry type and status
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.retryType = :retryType AND rs.status = :status")
    List<RetrySchedule> findByRetryTypeAndStatus(@Param("retryType") String retryType,
                                                 @Param("status") RetryStatus status);

    // Find expired retry schedules
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.expiresAt IS NOT NULL AND rs.expiresAt <= :now AND rs.status IN :statuses")
    List<RetrySchedule> findExpiredRetrySchedules(@Param("now") LocalDateTime now,
                                                  @Param("statuses") List<RetryStatus> statuses);

    // Find locked retry schedules
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.lockedBy IS NOT NULL AND rs.lockedAt IS NOT NULL")
    List<RetrySchedule> findLockedRetrySchedules();

    // Find by locked by (instance ID)
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.lockedBy = :lockedBy")
    List<RetrySchedule> findByLockedBy(@Param("lockedBy") String lockedBy);

    // Find with lock for processing (pessimistic lock)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rs FROM RetrySchedule rs WHERE rs.id = :id")
    Optional<RetrySchedule> findByIdWithLock(@Param("id") String id);
}

