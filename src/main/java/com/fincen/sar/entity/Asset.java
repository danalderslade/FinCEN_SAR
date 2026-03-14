package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "asset_type_id", nullable = false) Short assetTypeId;
    @Column(name = "asset_subtype_id", nullable = false) Short assetSubtypeId;
    @Column(name = "other_asset_subtype_text", length = 50) String otherAssetSubtypeText;
}
