package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "efiling_activity_acknowledgement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EfilingActivityAcknowledgement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    Activity activity;

    @Column(name = "bsa_identifier", length = 14) String bsaIdentifier;
    @Column(name = "status_code", length = 1) String statusCode;
    @Column(name = "acknowledged_at") OffsetDateTime acknowledgedAt;

    @Builder.Default
    @OneToMany(mappedBy = "acknowledgement", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EfilingActivityError> errors = new ArrayList<>();
}
