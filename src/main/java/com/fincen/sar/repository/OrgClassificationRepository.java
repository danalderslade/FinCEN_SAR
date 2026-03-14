package com.fincen.sar.repository;

import com.fincen.sar.entity.OrgClassification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgClassificationRepository extends JpaRepository<OrgClassification, Long> {
    List<OrgClassification> findByPartyId(Long partyId);
}
