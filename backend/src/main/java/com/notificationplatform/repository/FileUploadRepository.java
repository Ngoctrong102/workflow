package com.notificationplatform.repository;

import com.notificationplatform.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, String> {

    // Find by trigger ID
    List<FileUpload> findByTriggerId(String triggerId);

    // Find by status
    List<FileUpload> findByStatus(String status);

    // Find by trigger ID and status
    @Query("SELECT fu FROM FileUpload fu WHERE fu.trigger.id = :triggerId AND fu.status = :status")
    List<FileUpload> findByTriggerIdAndStatus(@Param("triggerId") String triggerId, 
                                              @Param("status") String status);

    // Find by uploaded date range
    @Query("SELECT fu FROM FileUpload fu WHERE fu.uploadedAt BETWEEN :startDate AND :endDate")
    List<FileUpload> findByUploadedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    // Find by trigger ID ordered by uploaded date
    @Query("SELECT fu FROM FileUpload fu WHERE fu.trigger.id = :triggerId ORDER BY fu.uploadedAt DESC")
    List<FileUpload> findByTriggerIdOrderByUploadedAtDesc(@Param("triggerId") String triggerId);

    // Find processing file uploads
    @Query("SELECT fu FROM FileUpload fu WHERE fu.status = 'processing'")
    List<FileUpload> findProcessingUploads();

    // Count by trigger ID
    long countByTriggerId(String triggerId);

    // Count by trigger ID and status
    @Query("SELECT COUNT(fu) FROM FileUpload fu WHERE fu.trigger.id = :triggerId AND fu.status = :status")
    long countByTriggerIdAndStatus(@Param("triggerId") String triggerId, 
                                  @Param("status") String status);
}

