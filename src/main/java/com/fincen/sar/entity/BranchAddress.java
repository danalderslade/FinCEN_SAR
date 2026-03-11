package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch_address",
    uniqueConstraints = @UniqueConstraint(columnNames = {"branch_party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_party_id", nullable = false)
    private BranchParty branchParty;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "raw_street_address1", length = 100) private String rawStreetAddress1;
    @Column(name = "raw_city",            length = 50)  private String rawCity;
    @Column(name = "raw_state_code",      length = 2)   private String rawStateCode;
    @Column(name = "raw_zip_code",        length = 9)   private String rawZipCode;
    @Column(name = "raw_country_code",    length = 2, nullable = false) private String rawCountryCode;
}
