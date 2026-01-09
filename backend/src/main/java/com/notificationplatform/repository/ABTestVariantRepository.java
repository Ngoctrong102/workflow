package com.notificationplatform.repository;

import com.notificationplatform.entity.ABTestVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ABTestVariantRepository extends JpaRepository<ABTestVariant, String> {

    @Query("SELECT v FROM ABTestVariant v WHERE v.id = :id AND v.deletedAt IS NULL")
    Optional<ABTestVariant> findByIdAndNotDeleted(@Param("id") String id);

    @Query("SELECT v FROM ABTestVariant v WHERE v.abTestId = :abTestId AND v.deletedAt IS NULL ORDER BY v.name")
    List<ABTestVariant> findByAbTestIdAndNotDeleted(@Param("abTestId") String abTestId);

    @Query("SELECT v FROM ABTestVariant v WHERE v.abTestId = :abTestId AND v.name = :name AND v.deletedAt IS NULL")
    Optional<ABTestVariant> findByAbTestIdAndNameAndNotDeleted(@Param("abTestId") String abTestId, @Param("name") String name);
}

