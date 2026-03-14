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
public class PartyAccountAssociationRequest {
    @NotNull
    private Long seqNum;

    @Builder.Default
    @Valid
    private List<AccountHoldingPartyRequest> accountHoldingParties = new ArrayList<>();
}
