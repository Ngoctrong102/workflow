package com.notificationplatform.repository;

import com.notificationplatform.entity.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, String> {

    @Query("SELECT n FROM InAppNotification n WHERE n.userId = :userId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<InAppNotification> findByUserIdAndNotDeleted(@Param("userId") String userId);

    @Query("SELECT n FROM InAppNotification n WHERE n.userId = :userId AND n.read = :read AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<InAppNotification> findByUserIdAndReadAndNotDeleted(@Param("userId") String userId, @Param("read") Boolean read);

    @Query("SELECT n FROM InAppNotification n WHERE n.userId = :userId AND n.deletedAt IS NULL AND (n.expiresAt IS NULL OR n.expiresAt > :now) ORDER BY n.createdAt DESC")
    List<InAppNotification> findActiveByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(n) FROM InAppNotification n WHERE n.userId = :userId AND n.read = false AND n.deletedAt IS NULL AND (n.expiresAt IS NULL OR n.expiresAt > :now)")
    long countUnreadByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);

    @Query("SELECT n FROM InAppNotification n WHERE n.id = :id AND n.deletedAt IS NULL")
    Optional<InAppNotification> findByIdAndNotDeleted(@Param("id") String id);

    @Query("SELECT n FROM InAppNotification n WHERE n.userId = :userId AND n.deletedAt IS NULL AND (n.expiresAt IS NULL OR n.expiresAt > :now) ORDER BY n.createdAt DESC")
    List<InAppNotification> findActiveByUserIdWithPagination(@Param("userId") String userId, @Param("now") LocalDateTime now);

    @Query("SELECT n FROM InAppNotification n WHERE n.channelId = :channelId AND n.deletedAt IS NULL")
    List<InAppNotification> findByChannelIdAndNotDeleted(@Param("channelId") String channelId);

    @Query("SELECT n FROM InAppNotification n WHERE n.workflowId = :workflowId AND n.deletedAt IS NULL")
    List<InAppNotification> findByWorkflowIdAndNotDeleted(@Param("workflowId") String workflowId);

    @Query("DELETE FROM InAppNotification n WHERE n.expiresAt < :now AND n.deletedAt IS NULL")
    void deleteExpired(@Param("now") LocalDateTime now);
}

