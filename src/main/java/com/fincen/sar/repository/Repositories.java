package com.fincen.sar.repository;

import com.fincen.sar.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EfilingBatchRepository extends JpaRepository<EfilingBatch, Long> {}

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByEfilingBatchId(Long batchId);

    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.activityAssociation " +
           "LEFT JOIN FETCH a.suspiciousActivity WHERE a.id = :id")
    Optional<Activity> findByIdWithDetails(@Param("id") Long id);
}

public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByActivityId(Long activityId);
    List<Party> findByActivityIdAndActivityPartyTypeCode(Long activityId, Short typeCode);
}

public interface SuspiciousActivityRepository extends JpaRepository<SuspiciousActivity, Long> {
    Optional<SuspiciousActivity> findByActivityId(Long activityId);
}

public interface ActivityNarrativeRepository extends JpaRepository<ActivityNarrative, Long> {
    List<ActivityNarrative> findByActivityIdOrderByNarrativeSequenceNumber(Long activityId);
}

public interface ActivityIpAddressRepository extends JpaRepository<ActivityIpAddress, Long> {
    List<ActivityIpAddress> findByActivityId(Long activityId);
}

public interface CyberEventIndicatorRepository extends JpaRepository<CyberEventIndicator, Long> {
    List<CyberEventIndicator> findByActivityId(Long activityId);
}

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByActivityId(Long activityId);
}

public interface AssetAttributeRepository extends JpaRepository<AssetAttribute, Long> {
    List<AssetAttribute> findByActivityId(Long activityId);
}

public interface PartyNameRepository extends JpaRepository<PartyName, Long> {
    List<PartyName> findByPartyId(Long partyId);
}

public interface PartyAddressRepository extends JpaRepository<PartyAddress, Long> {
    List<PartyAddress> findByPartyId(Long partyId);
}

public interface PartyPhoneRepository extends JpaRepository<PartyPhone, Long> {
    List<PartyPhone> findByPartyId(Long partyId);
}

public interface PartyIdentificationRepository extends JpaRepository<PartyIdentification, Long> {
    List<PartyIdentification> findByPartyId(Long partyId);
}

public interface PartyAssociationRepository extends JpaRepository<PartyAssociation, Long> {
    List<PartyAssociation> findByPartyId(Long partyId);
}

public interface BranchPartyRepository extends JpaRepository<BranchParty, Long> {
    List<BranchParty> findByPartyAssociationId(Long partyAssociationId);
}

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByAccountHoldingPartyId(Long accountHoldingPartyId);
}
