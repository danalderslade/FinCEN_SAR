package com.fincen.sar.repository;

import com.fincen.sar.entity.AssetAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetAttributeRepository extends JpaRepository<AssetAttribute, Long> {
    List<AssetAttribute> findByActivityId(Long activityId);
}
