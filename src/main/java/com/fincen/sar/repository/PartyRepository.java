package com.fincen.sar.repository;

import com.fincen.sar.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByActivityId(Long activityId);
    List<Party> findByActivityIdAndActivityPartyTypeCode(Long activityId, String activityPartyTypeCode);
}
