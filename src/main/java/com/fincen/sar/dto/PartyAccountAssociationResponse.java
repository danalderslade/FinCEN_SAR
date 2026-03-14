package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyAccountAssociationResponse {
    private Long id;
    private List<AccountHoldingPartyResponse> accountHoldingParties;
}
