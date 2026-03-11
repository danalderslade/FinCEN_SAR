package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "party_identification",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyIdentification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "party_identification_type_code")
    private Short partyIdentificationTypeCode;

    @Column(name = "party_identification_number", length = 25)
    private String partyIdentificationNumber;

    @Column(name = "tin_unknown")
    private Boolean tinUnknown;

    // Subject form-of-ID (Item 18)
    @Column(name = "identification_present_unknown") private Boolean identificationPresentUnknown;
    @Column(name = "other_issuer_country",   length = 2)  private String otherIssuerCountry;
    @Column(name = "other_issuer_state",     length = 2)  private String otherIssuerState;
    @Column(name = "other_party_identification_type_text", length = 50)
    private String otherPartyIdentificationTypeText;
}
