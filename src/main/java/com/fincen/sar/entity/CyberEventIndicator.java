package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "cyber_event_indicators",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CyberEventIndicator {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "cyber_event_indicators_type_code", nullable = false) Short cyberEventIndicatorsTypeCode;
    @Column(name = "event_value_text", nullable = false, columnDefinition = "TEXT") String eventValueText;
    @Column(name = "cyber_event_date") LocalDate cyberEventDate;
    @Column(name = "cyber_event_timestamp") LocalTime cyberEventTimestamp;
    @Column(name = "cyber_event_type_other_text", length = 50) String cyberEventTypeOtherText;
}
