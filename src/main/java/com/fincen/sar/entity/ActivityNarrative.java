package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_narrative_information",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"activity_id", "seq_num"}),
        @UniqueConstraint(columnNames = {"activity_id", "narrative_sequence_number"})
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityNarrative {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "narrative_sequence_number", nullable = false) Short narrativeSequenceNumber;
    @Column(name = "narrative_text", nullable = false, length = 4000) String narrativeText;
}
