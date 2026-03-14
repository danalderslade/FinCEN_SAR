package com.fincen.sar.repository;

import com.fincen.sar.entity.SuspiciousActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuspiciousActivityRepository extends JpaRepository<SuspiciousActivity, Long> {
    Optional<SuspiciousActivity> findByActivityId(Long activityId);
}
