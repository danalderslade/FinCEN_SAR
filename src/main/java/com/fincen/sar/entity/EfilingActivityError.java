package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "efiling_activity_error",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_acknowledgement_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EfilingActivityError {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_acknowledgement_id", nullable = false)
    EfilingActivityAcknowledgement acknowledgement;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "error_context_text", length = 4000) String errorContextText;
    @Column(name = "error_element_name_text", length = 512) String errorElementNameText;
    @Column(name = "error_level_text", length = 50) String errorLevelText;
    @Column(name = "error_text", length = 525) String errorText;
    @Column(name = "error_type_code", length = 50) String errorTypeCode;
}
