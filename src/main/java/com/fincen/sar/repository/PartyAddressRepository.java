package com.fincen.sar.repository;

import com.fincen.sar.entity.PartyAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyAddressRepository extends JpaRepository<PartyAddress, Long> {
    List<PartyAddress> findByPartyId(Long partyId);
}
