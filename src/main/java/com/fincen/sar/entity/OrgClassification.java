package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organization_classification_type_subtype",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrgClassification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "organization_type_id", nullable = false)
    private Short organizationTypeId;

    @Column(name = "organization_subtype_id")
    private Short organizationSubtypeId;

    @Column(name = "other_organization_type_text",    length = 50) private String otherOrganizationTypeText;
    @Column(name = "other_organization_subtype_text", length = 50) private String otherOrganizationSubtypeText;
}
