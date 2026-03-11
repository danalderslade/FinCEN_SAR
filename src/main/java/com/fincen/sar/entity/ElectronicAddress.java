package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "electronic_address",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ElectronicAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    /** E=Email, U=URL */
    @Column(name = "electronic_address_type_code", nullable = false, length = 1)
    private String electronicAddressTypeCode;

    @Column(name = "electronic_address_text", nullable = false, length = 517)
    private String electronicAddressText;
}
