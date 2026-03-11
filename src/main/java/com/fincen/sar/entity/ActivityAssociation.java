package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_association")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityAssociation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Builder.Default
    @Column(name = "initial_report_indicator", nullable = false)
    private Boolean initialReportIndicator = false;

    @Builder.Default
    @Column(name = "corrects_amends_prior_report", nullable = false)
    private Boolean correctsAmendsPriorReport = false;

    @Builder.Default
    @Column(name = "continuing_activity_report", nullable = false)
    private Boolean continuingActivityReport = false;

    @Builder.Default
    @Column(name = "joint_report_indicator", nullable = false)
    private Boolean jointReportIndicator = false;
}
