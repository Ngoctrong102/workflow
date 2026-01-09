package com.notificationplatform.repository;

import com.notificationplatform.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {

    // Find active workflows (not soft deleted)
    @Query("SELECT w FROM Workflow w WHERE w.deletedAt IS NULL")
    List<Workflow> findAllActive();

    // Find by status (excluding soft deleted)
    @Query("SELECT w FROM Workflow w WHERE w.status = :status AND w.deletedAt IS NULL")
    List<Workflow> findByStatus(@Param("status") String status);

    // Find by status and not soft deleted
    @Query("SELECT w FROM Workflow w WHERE w.status = :status AND w.deletedAt IS NULL ORDER BY w.createdAt DESC")
    List<Workflow> findByStatusOrderByCreatedAtDesc(@Param("status") String status);

    // Find by ID and not soft deleted
    @Query("SELECT w FROM Workflow w WHERE w.id = :id AND w.deletedAt IS NULL")
    Optional<Workflow> findByIdAndNotDeleted(@Param("id") String id);

    // Find by created date range
    @Query("SELECT w FROM Workflow w WHERE w.createdAt BETWEEN :startDate AND :endDate AND w.deletedAt IS NULL")
    List<Workflow> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Count active workflows by status
    @Query("SELECT COUNT(w) FROM Workflow w WHERE w.status = :status AND w.deletedAt IS NULL")
    long countByStatus(@Param("status") String status);

    // Check if workflow exists and is not deleted
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Workflow w WHERE w.id = :id AND w.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") String id);

    // Find by status and not soft deleted
    @Query("SELECT w FROM Workflow w WHERE w.status = :status AND w.deletedAt IS NULL")
    List<Workflow> findByStatusAndDeletedAtIsNull(@Param("status") com.notificationplatform.entity.enums.WorkflowStatus status);

    // Search by name or description (excluding soft deleted)
    @Query("SELECT w FROM Workflow w WHERE (LOWER(w.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND w.deletedAt IS NULL")
    List<Workflow> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    // Find by tag (excluding soft deleted)
    @Query(value = "SELECT * FROM workflows WHERE :tag = ANY(tags) AND deleted_at IS NULL", nativeQuery = true)
    List<Workflow> findByTag(@Param("tag") String tag);
}

