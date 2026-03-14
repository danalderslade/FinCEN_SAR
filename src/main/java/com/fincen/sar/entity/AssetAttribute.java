package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets_attribute",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetAttribute {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "asset_attribute_type_id", nullable = false) Short assetAttributeTypeId;
    @Column(name = "asset_attribute_description_text", nullable = false, length = 50) String assetAttributeDescriptionText;
}
