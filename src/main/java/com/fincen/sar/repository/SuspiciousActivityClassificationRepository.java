package com.fincen.sar.repository;

import com.fincen.sar.entity.SuspiciousActivityClassification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuspiciousActivityClassificationRepository
        extends JpaRepository<SuspiciousActivityClassification, Long> {
    List<SuspiciousActivityClassification> findBySuspiciousActivityId(Long saId);
}
