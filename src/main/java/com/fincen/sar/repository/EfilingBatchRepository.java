package com.fincen.sar.repository;

import com.fincen.sar.entity.EfilingBatch;
import com.fincen.sar.entity.FilingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EfilingBatchRepository extends JpaRepository<EfilingBatch, Long> {
    Page<EfilingBatch> findByFilingStatus(FilingStatus status, Pageable pageable);

    long countByFilingStatus(FilingStatus status);

    @Query("SELECT COALESCE(SUM(b.activityCount), 0) FROM EfilingBatch b")
    long sumActivityCount();

    @Query("SELECT COALESCE(SUM(b.partyCount), 0) FROM EfilingBatch b")
    long sumPartyCount();
}
