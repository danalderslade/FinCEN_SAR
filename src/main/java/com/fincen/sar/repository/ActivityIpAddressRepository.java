package com.fincen.sar.repository;

import com.fincen.sar.entity.ActivityIpAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityIpAddressRepository extends JpaRepository<ActivityIpAddress, Long> {
    List<ActivityIpAddress> findByActivityId(Long activityId);
}
