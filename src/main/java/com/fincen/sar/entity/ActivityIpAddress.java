package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "activity_ip_address",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityIpAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "ip_address_text", nullable = false, length = 45) String ipAddressText;
    @Column(name = "ip_address_date") LocalDate ipAddressDate;
    @Column(name = "ip_address_timestamp") LocalTime ipAddressTimestamp;
}
