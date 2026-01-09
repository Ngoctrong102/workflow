package com.notificationplatform.repository;

import com.notificationplatform.entity.DeadLetterQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeadLetterQueueRepository extends JpaRepository<DeadLetterQueue, String> {

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.id = :id")
    Optional<DeadLetterQueue> findById(@Param("id") String id);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.sourceType = :sourceType AND d.sourceId = :sourceId")
    Optional<DeadLetterQueue> findBySourceTypeAndSourceId(@Param("sourceType") String sourceType,
                                                          @Param("sourceId") String sourceId);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.status = :status")
    List<DeadLetterQueue> findByStatus(@Param("status") String status);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.status = 'pending' AND " +
           "(d.nextRetryAt IS NULL OR d.nextRetryAt <= :now)")
    List<DeadLetterQueue> findPendingRetries(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.channelId = :channelId")
    List<DeadLetterQueue> findByChannelId(@Param("channelId") String channelId);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.workflowId = :workflowId")
    List<DeadLetterQueue> findByWorkflowId(@Param("workflowId") String workflowId);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.errorType = :errorType")
    List<DeadLetterQueue> findByErrorType(@Param("errorType") String errorType);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.createdAt >= :startDate AND d.createdAt <= :endDate")
    List<DeadLetterQueue> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM DeadLetterQueue d WHERE d.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(d) FROM DeadLetterQueue d WHERE d.channelId = :channelId")
    long countByChannelId(@Param("channelId") String channelId);
}

