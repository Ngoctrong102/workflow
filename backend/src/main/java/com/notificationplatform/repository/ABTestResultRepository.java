package com.notificationplatform.repository;

import com.notificationplatform.entity.ABTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ABTestResultRepository extends JpaRepository<ABTestResult, String> {

    @Query("SELECT r FROM ABTestResult r WHERE r.id = :id")
    Optional<ABTestResult> findById(@Param("id") String id);

    @Query("SELECT r FROM ABTestResult r WHERE r.abTestId = :abTestId")
    List<ABTestResult> findByAbTestId(@Param("abTestId") String abTestId);

    @Query("SELECT r FROM ABTestResult r WHERE r.variantId = :variantId")
    List<ABTestResult> findByVariantId(@Param("variantId") String variantId);

    @Query("SELECT r FROM ABTestResult r WHERE r.abTestId = :abTestId AND r.variantId = :variantId")
    List<ABTestResult> findByAbTestIdAndVariantId(@Param("abTestId") String abTestId, @Param("variantId") String variantId);

    @Query("SELECT r FROM ABTestResult r WHERE r.userId = :userId AND r.abTestId = :abTestId")
    Optional<ABTestResult> findByUserIdAndAbTestId(@Param("userId") String userId, @Param("abTestId") String abTestId);

    @Query("SELECT COUNT(r) FROM ABTestResult r WHERE r.abTestId = :abTestId AND r.variantId = :variantId")
    long countByAbTestIdAndVariantId(@Param("abTestId") String abTestId, @Param("variantId") String variantId);

    @Query("SELECT COUNT(r) FROM ABTestResult r WHERE r.abTestId = :abTestId AND r.variantId = :variantId AND r.openedAt IS NOT NULL")
    long countOpensByAbTestIdAndVariantId(@Param("abTestId") String abTestId, @Param("variantId") String variantId);

    @Query("SELECT COUNT(r) FROM ABTestResult r WHERE r.abTestId = :abTestId AND r.variantId = :variantId AND r.clickedAt IS NOT NULL")
    long countClicksByAbTestIdAndVariantId(@Param("abTestId") String abTestId, @Param("variantId") String variantId);

    @Query("SELECT COUNT(r) FROM ABTestResult r WHERE r.abTestId = :abTestId AND r.variantId = :variantId AND r.convertedAt IS NOT NULL")
    long countConversionsByAbTestIdAndVariantId(@Param("abTestId") String abTestId, @Param("variantId") String variantId);

    @Query("SELECT r FROM ABTestResult r WHERE r.abTestId = :abTestId AND r.assignedAt >= :startDate AND r.assignedAt <= :endDate")
    List<ABTestResult> findByAbTestIdAndDateRange(@Param("abTestId") String abTestId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
}

