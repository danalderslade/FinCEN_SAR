package com.fincen.sar.repository;

import com.fincen.sar.entity.ActivityNarrative;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActivityNarrativeRepository extends JpaRepository<ActivityNarrative, Long> {
    List<ActivityNarrative> findByActivityIdOrderByNarrativeSequenceNumber(Long activityId);
    Optional<ActivityNarrative> findByActivityIdAndNarrativeSequenceNumber(Long activityId, Short seqNum);
}
