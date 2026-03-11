package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "party_occupation_business")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyOccupation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false, unique = true)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "naics_code",             length = 6)  private String naicsCode;
    @Column(name = "occupation_business_text", length = 50) private String occupationBusinessText;
}
