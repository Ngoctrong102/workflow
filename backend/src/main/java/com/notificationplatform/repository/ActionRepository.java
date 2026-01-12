package com.notificationplatform.repository;

import com.notificationplatform.entity.Action;
import com.notificationplatform.entity.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Action entity - Stores action definitions in the registry.
 */
@Repository
public interface ActionRepository extends JpaRepository<Action, String> {

    // Find active actions (not soft deleted)
    @Query("SELECT a FROM Action a WHERE a.deletedAt IS NULL")
    List<Action> findAllActive();

    // Find enabled actions (not soft deleted)
    @Query("SELECT a FROM Action a WHERE a.enabled = true AND a.deletedAt IS NULL")
    List<Action> findAllEnabled();

    // Find by type (excluding soft deleted)
    @Query("SELECT a FROM Action a WHERE a.type = :type AND a.deletedAt IS NULL")
    List<Action> findByType(@Param("type") ActionType type);

    // Find by type and enabled (excluding soft deleted)
    @Query("SELECT a FROM Action a WHERE a.type = :type AND a.enabled = true AND a.deletedAt IS NULL")
    List<Action> findByTypeAndEnabled(@Param("type") ActionType type);

    // Find by ID and not soft deleted
    @Query("SELECT a FROM Action a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Action> findByIdAndNotDeleted(@Param("id") String id);

    // Check if action exists and is not deleted
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Action a WHERE a.id = :id AND a.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") String id);
}

