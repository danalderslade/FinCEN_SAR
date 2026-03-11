package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "efiling_batch")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EfilingBatch {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_count", nullable = false)
    private Integer activityCount;

    @Column(name = "total_amount", precision = 18)
    private BigDecimal totalAmount;

    @Column(name = "party_count", nullable = false)
    private Integer partyCount;

    @Builder.Default
    @Column(name = "activity_attachment_count", nullable = false)
    private Integer activityAttachmentCount = 0;

    @Builder.Default
    @Column(name = "attachment_count", nullable = false)
    private Integer attachmentCount = 0;

    @Builder.Default
    @Column(name = "form_type_code", nullable = false, length = 4)
    private String formTypeCode = "SARX";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "efilingBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();
}
