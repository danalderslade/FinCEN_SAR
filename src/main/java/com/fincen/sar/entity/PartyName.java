package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

// ─────────────────────────────────────────────────────────────────────────────
// party_name
// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "party_name",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyName {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    Party party;

    @Column(name = "seq_num", nullable = false)
    Long seqNum;

    /** L / DBA / AKA */
    @Column(name = "party_name_type_code", nullable = false, length = 3)
    String partyNameTypeCode;

    // non-Subject
    @Column(name = "raw_party_full_name", length = 150)
    String rawPartyFullName;

    // Subject individual name parts
    @Column(name = "entity_last_name_unknown")
    Boolean entityLastNameUnknown;

    @Column(name = "first_name_unknown")
    Boolean firstNameUnknown;

    @Column(name = "raw_entity_individual_last_name", length = 150)
    String rawEntityIndividualLastName;

    @Column(name = "raw_individual_first_name", length = 35)
    String rawIndividualFirstName;

    @Column(name = "raw_individual_middle_name", length = 35)
    String rawIndividualMiddleName;

    @Column(name = "raw_individual_name_suffix_text", length = 35)
    String rawIndividualNameSuffixText;
}
