package com.fincen.sar.repository;

import com.fincen.sar.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByAccountHoldingPartyId(Long accountHoldingPartyId);
}
