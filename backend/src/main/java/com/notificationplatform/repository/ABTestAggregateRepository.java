package com.notificationplatform.repository;

import com.notificationplatform.entity.ABTestAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ABTestAggregateRepository extends JpaRepository<ABTestAggregate, String> {

    @Query("SELECT a FROM ABTestAggregate a WHERE a.abTestId = :abTestId AND a.variantId = :variantId AND a.date = :date")
    Optional<ABTestAggregate> findByAbTestIdAndVariantIdAndDate(@Param("abTestId") String abTestId, 
                                                                  @Param("variantId") String variantId, 
                                                                  @Param("date") LocalDate date);

    @Query("SELECT a FROM ABTestAggregate a WHERE a.abTestId = :abTestId AND a.date >= :startDate AND a.date <= :endDate ORDER BY a.date, a.variantId")
    List<ABTestAggregate> findByAbTestIdAndDateRange(@Param("abTestId") String abTestId, 
                                                      @Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM ABTestAggregate a WHERE a.abTestId = :abTestId ORDER BY a.date DESC, a.variantId")
    List<ABTestAggregate> findByAbTestId(@Param("abTestId") String abTestId);

    @Query("SELECT a FROM ABTestAggregate a WHERE a.abTestId = :abTestId AND a.variantId = :variantId ORDER BY a.date DESC")
    List<ABTestAggregate> findByAbTestIdAndVariantId(@Param("abTestId") String abTestId, @Param("variantId") String variantId);
}

