package com.fincen.sar.repository;

import com.fincen.sar.entity.PartyIdentification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyIdentificationRepository extends JpaRepository<PartyIdentification, Long> {
    List<PartyIdentification> findByPartyId(Long partyId);
}
