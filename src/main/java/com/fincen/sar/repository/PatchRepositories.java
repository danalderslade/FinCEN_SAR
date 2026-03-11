package com.fincen.sar.repository;

import com.fincen.sar.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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

public interface OrgClassificationRepository extends JpaRepository<OrgClassification, Long> {
    List<OrgClassification> findByPartyId(Long partyId);
}

public interface PartyOccupationRepository extends JpaRepository<PartyOccupation, Long> {
    Optional<PartyOccupation> findByPartyId(Long partyId);
}

public interface ElectronicAddressRepository extends JpaRepository<ElectronicAddress, Long> {
    List<ElectronicAddress> findByPartyId(Long partyId);
}

public interface PartyAssociationRepository extends JpaRepository<PartyAssociation, Long> {
    List<PartyAssociation> findByPartyId(Long partyId);
}

public interface BranchPartyRepository extends JpaRepository<BranchParty, Long> {
    List<BranchParty> findByPartyAssociationId(Long partyAssociationId);
}

public interface BranchAddressRepository extends JpaRepository<BranchAddress, Long> {}

public interface SuspiciousActivityClassificationRepository
        extends JpaRepository<SuspiciousActivityClassification, Long> {
    List<SuspiciousActivityClassification> findBySuspiciousActivityId(Long saId);
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

public interface PartyAccountAssociationRepository extends JpaRepository<PartyAccountAssociation, Long> {
    Optional<PartyAccountAssociation> findByPartyId(Long partyId);
}
