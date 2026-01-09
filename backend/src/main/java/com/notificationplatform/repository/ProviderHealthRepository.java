package com.notificationplatform.repository;

import com.notificationplatform.entity.ProviderHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderHealthRepository extends JpaRepository<ProviderHealth, String> {

    @Query("SELECT p FROM ProviderHealth p WHERE p.channelId = :channelId")
    Optional<ProviderHealth> findByChannelId(@Param("channelId") String channelId);

    @Query("SELECT p FROM ProviderHealth p WHERE p.channelType = :channelType")
    List<ProviderHealth> findByChannelType(@Param("channelType") String channelType);

    @Query("SELECT p FROM ProviderHealth p WHERE p.status = :status")
    List<ProviderHealth> findByStatus(@Param("status") String status);

    @Query("SELECT p FROM ProviderHealth p WHERE p.status IN ('degraded', 'down')")
    List<ProviderHealth> findUnhealthyProviders();
}

