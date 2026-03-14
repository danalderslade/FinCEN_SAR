package com.fincen.sar.repository;

import com.fincen.sar.entity.CyberEventIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CyberEventIndicatorRepository extends JpaRepository<CyberEventIndicator, Long> {
    List<CyberEventIndicator> findByActivityId(Long activityId);
}
