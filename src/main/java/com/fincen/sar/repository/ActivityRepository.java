package com.fincen.sar.repository;

import com.fincen.sar.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByEfilingBatchId(Long batchId);

    @Query("SELECT a FROM Activity a "
         + "LEFT JOIN FETCH a.activityAssociation "
         + "LEFT JOIN FETCH a.activitySupportDocument "
         + "LEFT JOIN FETCH a.suspiciousActivity "
         + "LEFT JOIN FETCH a.parties "
         + "WHERE a.id = :id")
    Optional<Activity> findByIdWithDetails(@Param("id") Long id);
}
