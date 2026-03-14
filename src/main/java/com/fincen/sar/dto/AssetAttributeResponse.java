package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetAttributeResponse {
    private Long id;
    private Short assetAttributeTypeId;
    private String assetAttributeDescriptionText;
}
