package com.fincen.sar.repository;

import com.fincen.sar.entity.PartyAssociation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyAssociationRepository extends JpaRepository<PartyAssociation, Long> {
    List<PartyAssociation> findByPartyId(Long partyId);
}
