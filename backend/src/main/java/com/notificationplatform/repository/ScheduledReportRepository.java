package com.notificationplatform.repository;

import com.notificationplatform.entity.ScheduledReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, String> {

    @Query("SELECT s FROM ScheduledReport s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<ScheduledReport> findByIdAndNotDeleted(@Param("id") String id);

    @Query("SELECT s FROM ScheduledReport s WHERE s.deletedAt IS NULL")
    List<ScheduledReport> findAllActive();

    @Query("SELECT s FROM ScheduledReport s WHERE s.status = 'active' AND s.deletedAt IS NULL")
    List<ScheduledReport> findActiveSchedules();

    @Query("SELECT s FROM ScheduledReport s WHERE s.status = 'active' AND " +
           "(s.nextRunAt IS NULL OR s.nextRunAt <= :now) AND s.deletedAt IS NULL")
    List<ScheduledReport> findDueSchedules(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM ScheduledReport s WHERE s.reportType = :reportType AND s.deletedAt IS NULL")
    List<ScheduledReport> findByReportType(@Param("reportType") String reportType);

    @Query("SELECT s FROM ScheduledReport s WHERE s.status = :status AND s.deletedAt IS NULL")
    List<ScheduledReport> findByStatus(@Param("status") String status);
}

