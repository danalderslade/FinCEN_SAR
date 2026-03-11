package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "city_unknown")        private Boolean cityUnknown;
    @Column(name = "country_code_unknown") private Boolean countryCodeUnknown;
    @Column(name = "state_code_unknown")  private Boolean stateCodeUnknown;
    @Column(name = "street_address_unknown") private Boolean streetAddressUnknown;
    @Column(name = "zip_code_unknown")    private Boolean zipCodeUnknown;

    @Column(name = "raw_street_address1", length = 100) private String rawStreetAddress1;
    @Column(name = "raw_city",            length = 50)  private String rawCity;
    @Column(name = "raw_state_code",      length = 2)   private String rawStateCode;
    @Column(name = "raw_zip_code",        length = 9)   private String rawZipCode;
    @Column(name = "raw_country_code",    length = 2)   private String rawCountryCode;
}
