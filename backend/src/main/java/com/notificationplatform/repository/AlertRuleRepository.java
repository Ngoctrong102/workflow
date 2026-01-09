package com.notificationplatform.repository;

import com.notificationplatform.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, String> {

    @Query("SELECT a FROM AlertRule a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<AlertRule> findByIdAndNotDeleted(@Param("id") String id);

    @Query("SELECT a FROM AlertRule a WHERE a.deletedAt IS NULL")
    List<AlertRule> findAllActive();

    @Query("SELECT a FROM AlertRule a WHERE a.enabled = true AND a.deletedAt IS NULL")
    List<AlertRule> findEnabledRules();

    @Query("SELECT a FROM AlertRule a WHERE a.ruleType = :ruleType AND a.enabled = true AND a.deletedAt IS NULL")
    List<AlertRule> findByRuleType(@Param("ruleType") String ruleType);

    @Query("SELECT a FROM AlertRule a WHERE a.channelType = :channelType AND a.enabled = true AND a.deletedAt IS NULL")
    List<AlertRule> findByChannelType(@Param("channelType") String channelType);
}

