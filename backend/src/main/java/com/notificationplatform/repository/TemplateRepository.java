package com.notificationplatform.repository;

import com.notificationplatform.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, String> {

    // Find active templates (not soft deleted)
    @Query("SELECT t FROM Template t WHERE t.deletedAt IS NULL")
    List<Template> findAllActive();

    // Find by channel (excluding soft deleted)
    @Query("SELECT t FROM Template t WHERE t.channel = :channel AND t.deletedAt IS NULL")
    List<Template> findByChannel(@Param("channel") String channel);

    // Find by channel and category
    @Query("SELECT t FROM Template t WHERE t.channel = :channel AND t.category = :category AND t.deletedAt IS NULL")
    List<Template> findByChannelAndCategory(@Param("channel") String channel, 
                                            @Param("category") String category);

    // Find by ID and not soft deleted
    @Query("SELECT t FROM Template t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Template> findByIdAndNotDeleted(@Param("id") String id);

    // Find by created date range
    @Query("SELECT t FROM Template t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL")
    List<Template> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    // Count templates by channel
    @Query("SELECT COUNT(t) FROM Template t WHERE t.channel = :channel AND t.deletedAt IS NULL")
    long countByChannel(@Param("channel") String channel);

    // Check if template exists and is not deleted
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Template t WHERE t.id = :id AND t.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") String id);
}

