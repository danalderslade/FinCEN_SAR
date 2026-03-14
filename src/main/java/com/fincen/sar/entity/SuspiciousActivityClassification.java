package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suspicious_activity_classification",
    uniqueConstraints = @UniqueConstraint(columnNames = {"suspicious_activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SuspiciousActivityClassification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suspicious_activity_id", nullable = false)
    SuspiciousActivity suspiciousActivity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "suspicious_activity_type_id", nullable = false) Short suspiciousActivityTypeId;
    @Column(name = "suspicious_activity_subtype_id", nullable = false) Short suspiciousActivitySubtypeId;
    @Column(name = "other_suspicious_activity_type_text", length = 50) String otherSuspiciousActivityTypeText;
}
