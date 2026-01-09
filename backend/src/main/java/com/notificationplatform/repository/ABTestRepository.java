package com.notificationplatform.repository;

import com.notificationplatform.entity.ABTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ABTestRepository extends JpaRepository<ABTest, String> {

    @Query("SELECT t FROM ABTest t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<ABTest> findByIdAndNotDeleted(@Param("id") String id);

    @Query("SELECT t FROM ABTest t WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<ABTest> findAllActive();

    @Query("SELECT t FROM ABTest t WHERE t.workflowId = :workflowId AND t.deletedAt IS NULL")
    List<ABTest> findByWorkflowIdAndNotDeleted(@Param("workflowId") String workflowId);

    @Query("SELECT t FROM ABTest t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<ABTest> findByStatusAndNotDeleted(@Param("status") String status);

    @Query("SELECT t FROM ABTest t WHERE t.status = 'running' AND t.deletedAt IS NULL")
    List<ABTest> findRunningTests();
}

