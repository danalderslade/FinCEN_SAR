package com.fincen.sar.repository;

import com.fincen.sar.entity.ElectronicAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElectronicAddressRepository extends JpaRepository<ElectronicAddress, Long> {
    List<ElectronicAddress> findByPartyId(Long partyId);
}
