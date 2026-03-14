package com.fincen.sar.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AssetAttributeRequest {
    @NotNull
    private Long seqNum;

    @NotNull
    private Short assetAttributeTypeId;

    @NotBlank
    @Size(max = 50)
    private String assetAttributeDescriptionText;
}
