package com.fincen.sar.repository;

import com.fincen.sar.entity.PartyOccupation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyOccupationRepository extends JpaRepository<PartyOccupation, Long> {
    Optional<PartyOccupation> findByPartyId(Long partyId);
}
