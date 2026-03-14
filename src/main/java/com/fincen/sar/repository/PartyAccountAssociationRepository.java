package com.fincen.sar.repository;

import com.fincen.sar.entity.PartyAccountAssociation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyAccountAssociationRepository extends JpaRepository<PartyAccountAssociation, Long> {
    Optional<PartyAccountAssociation> findByPartyId(Long partyId);
}
