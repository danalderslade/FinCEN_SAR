package com.fincen.sar.repository;

import com.fincen.sar.entity.BranchParty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchPartyRepository extends JpaRepository<BranchParty, Long> {
    List<BranchParty> findByPartyAssociationId(Long partyAssociationId);
}
