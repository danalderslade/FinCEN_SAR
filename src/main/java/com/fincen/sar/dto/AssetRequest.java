package com.fincen.sar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetRequest {
    @NotNull
    private Long seqNum;

    @NotNull
    private Short assetTypeId;

    @NotNull
    private Short assetSubtypeId;

    @Size(max = 50)
    private String otherAssetSubtypeText;
}
