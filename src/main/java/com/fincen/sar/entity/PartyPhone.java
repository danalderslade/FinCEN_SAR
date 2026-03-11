package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "phone_number",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyPhone {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "phone_number_text",      length = 16) private String phoneNumberText;
    @Column(name = "phone_number_extension", length = 6)  private String phoneNumberExtension;
    /** R/W/M/F — Subject only */
    @Column(name = "phone_number_type_code", length = 1)  private String phoneNumberTypeCode;
}
