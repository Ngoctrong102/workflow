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

    // Find by trigger type (excluding soft deleted)
    @Query("SELECT t FROM Trigger t WHERE t.triggerType = :triggerType AND t.deletedAt IS NULL")
    List<Trigger> findByTriggerType(@Param("triggerType") TriggerType triggerType);

    // Find by status (excluding soft deleted)
    @Query("SELECT t FROM Trigger t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<Trigger> findByStatus(@Param("status") com.notificationplatform.entity.enums.TriggerStatus status);

    // Find by ID and not soft deleted
    @Query("SELECT t FROM Trigger t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Trigger> findByIdAndNotDeleted(@Param("id") String id);

    // Find by created date range
    @Query("SELECT t FROM Trigger t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL")
    List<Trigger> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    // Check if trigger exists and is not deleted
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Trigger t WHERE t.id = :id AND t.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") String id);

    // Find by path, method and active status (for API triggers)
    @Query(value = "SELECT * FROM triggers WHERE config::text LIKE CONCAT('%\"path\":\"', :path, '\"%') AND config::text LIKE CONCAT('%\"method\":\"', :method, '\"%') AND status = 'active' AND deleted_at IS NULL", nativeQuery = true)
    List<Trigger> findByPathAndMethodAndActive(@Param("path") String path, @Param("method") String method);

    // Find by trigger type and active status
    @Query("SELECT t FROM Trigger t WHERE t.triggerType = :triggerType AND t.status = 'active' AND t.deletedAt IS NULL")
    List<Trigger> findByTriggerTypeAndActive(@Param("triggerType") TriggerType triggerType);

    // Find by trigger type and status
    @Query("SELECT t FROM Trigger t WHERE t.triggerType = :triggerType AND t.status = :status AND t.deletedAt IS NULL")
    List<Trigger> findByTriggerTypeAndStatus(@Param("triggerType") TriggerType triggerType, 
                                             @Param("status") com.notificationplatform.entity.enums.TriggerStatus status);
}

