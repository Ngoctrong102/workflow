package com.notificationplatform.repository;

import com.notificationplatform.entity.RateLimitTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RateLimitTrackingRepository extends JpaRepository<RateLimitTracking, String> {

    @Query("SELECT r FROM RateLimitTracking r WHERE r.channelId = :channelId " +
           "AND r.windowStart <= :now AND r.windowEnd >= :now AND r.windowType = :windowType")
    Optional<RateLimitTracking> findCurrentWindow(@Param("channelId") String channelId,
                                                  @Param("now") LocalDateTime now,
                                                  @Param("windowType") String windowType);

    @Query("SELECT r FROM RateLimitTracking r WHERE r.channelId = :channelId " +
           "AND r.windowStart >= :startDate AND r.windowStart <= :endDate")
    List<RateLimitTracking> findByChannelIdAndDateRange(@Param("channelId") String channelId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(r.requestCount) FROM RateLimitTracking r WHERE r.channelId = :channelId " +
           "AND r.windowStart >= :startDate AND r.windowStart <= :endDate")
    Long sumRequestCountByChannelIdAndDateRange(@Param("channelId") String channelId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
}

