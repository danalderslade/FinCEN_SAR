package com.fincen.sar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchPartyRequest {
    @NotNull
    private Long seqNum;

    private Boolean sellingLocationIndicator;
    private Boolean payLocationIndicator;
    private Boolean sellingPayingLocationIndicator;

    @Builder.Default
    @Valid
    private List<BranchAddressRequest> addresses = new ArrayList<>();

    @Valid
    private BranchIdentificationRequest identification;
}