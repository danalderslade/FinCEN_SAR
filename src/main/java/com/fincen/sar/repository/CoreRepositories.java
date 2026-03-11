package com.fincen.sar.repository;

import com.fincen.sar.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EfilingBatchRepository extends JpaRepository<EfilingBatch, Long> {}

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByEfilingBatchId(Long batchId);
}

public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByActivityId(Long activityId);
}

public interface SuspiciousActivityRepository extends JpaRepository<SuspiciousActivity, Long> {
    Optional<SuspiciousActivity> findByActivityId(Long activityId);
}
