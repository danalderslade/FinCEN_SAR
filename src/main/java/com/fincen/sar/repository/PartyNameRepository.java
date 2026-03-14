package com.fincen.sar.repository;

import com.fincen.sar.entity.PartyName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyNameRepository extends JpaRepository<PartyName, Long> {
    List<PartyName> findByPartyId(Long partyId);
}
