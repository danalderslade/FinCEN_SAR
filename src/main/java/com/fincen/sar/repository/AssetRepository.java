package com.fincen.sar.repository;

import com.fincen.sar.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByActivityId(Long activityId);
}
