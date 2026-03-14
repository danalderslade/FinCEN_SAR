package com.fincen.sar.repository;

import com.fincen.sar.entity.PartyPhone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyPhoneRepository extends JpaRepository<PartyPhone, Long> {
    List<PartyPhone> findByPartyId(Long partyId);
}
