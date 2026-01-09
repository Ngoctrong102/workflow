package com.notificationplatform.repository;

import com.notificationplatform.entity.ObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectTypeRepository extends JpaRepository<ObjectType, String> {

    /**
     * Find all active object types (not soft deleted).
     * @return List of active object types
     */
    @Query("SELECT ot FROM ObjectType ot WHERE ot.deletedAt IS NULL")
    List<ObjectType> findAllActive();

    /**
     * Find object type by ID if not deleted.
     * @param id The object type ID
     * @return Optional ObjectType
     */
    @Query("SELECT ot FROM ObjectType ot WHERE ot.id = :id AND ot.deletedAt IS NULL")
    Optional<ObjectType> findByIdAndNotDeleted(@Param("id") String id);

    /**
     * Find object type by name if not deleted.
     * @param name The object type name
     * @return Optional ObjectType
     */
    @Query("SELECT ot FROM ObjectType ot WHERE ot.name = :name AND ot.deletedAt IS NULL")
    Optional<ObjectType> findByNameAndNotDeleted(@Param("name") String name);

    /**
     * Find object types by tag (using array contains).
     * @param tag The tag to search for
     * @return List of object types containing the tag
     */
    @Query(value = "SELECT * FROM object_types WHERE :tag = ANY(tags) AND deleted_at IS NULL", nativeQuery = true)
    List<ObjectType> findByTagsContaining(@Param("tag") String tag);

    /**
     * Search object types by name or description.
     * @param searchTerm The search term
     * @return List of matching object types
     */
    @Query("SELECT ot FROM ObjectType ot WHERE " +
           "(LOWER(ot.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ot.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND ot.deletedAt IS NULL")
    List<ObjectType> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
}

