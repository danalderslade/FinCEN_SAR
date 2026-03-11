package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suspicious_activity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SuspiciousActivity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "amount_unknown")    private Boolean amountUnknown;
    @Column(name = "no_amount_involved") private Boolean noAmountInvolved;

    @Column(name = "total_suspicious_amount", precision = 15)
    private BigDecimal totalSuspiciousAmount;

    @Column(name = "suspicious_activity_from_date", nullable = false)
    private LocalDate suspiciousActivityFromDate;

    @Column(name = "suspicious_activity_to_date")
    private LocalDate suspiciousActivityToDate;

    @Column(name = "cumulative_total_violation_amount", precision = 15)
    private BigDecimal cumulativeTotalViolationAmount;

    @Builder.Default
    @OneToMany(mappedBy = "suspiciousActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SuspiciousActivityClassification> classifications = new ArrayList<>();
}
