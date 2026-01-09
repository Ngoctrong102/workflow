package com.notificationplatform.repository;

import com.notificationplatform.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, String> {

    // Find active channels (not soft deleted)
    @Query("SELECT c FROM Channel c WHERE c.deletedAt IS NULL")
    List<Channel> findAllActive();

    // Find by type (excluding soft deleted)
    @Query("SELECT c FROM Channel c WHERE c.type = :type AND c.deletedAt IS NULL")
    List<Channel> findByType(@Param("type") String type);

    // Find by status (excluding soft deleted)
    @Query("SELECT c FROM Channel c WHERE c.status = :status AND c.deletedAt IS NULL")
    List<Channel> findByStatus(@Param("status") String status);

    // Find by type and status (excluding soft deleted)
    @Query("SELECT c FROM Channel c WHERE c.type = :type AND c.status = :status AND c.deletedAt IS NULL")
    List<Channel> findByTypeAndStatus(@Param("type") String type, @Param("status") String status);

    // Find active channels by type
    @Query("SELECT c FROM Channel c WHERE c.type = :type AND c.status = 'active' AND c.deletedAt IS NULL")
    List<Channel> findActiveByType(@Param("type") String type);

    // Find by ID and not soft deleted
    @Query("SELECT c FROM Channel c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Channel> findByIdAndNotDeleted(@Param("id") String id);

    // Find by created date range
    @Query("SELECT c FROM Channel c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.deletedAt IS NULL")
    List<Channel> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Count channels by type
    @Query("SELECT COUNT(c) FROM Channel c WHERE c.type = :type AND c.deletedAt IS NULL")
    long countByType(@Param("type") String type);

    // Check if channel exists and is not deleted
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Channel c WHERE c.id = :id AND c.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") String id);
}

