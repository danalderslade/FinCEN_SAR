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
public class AccountHoldingPartyIdentificationRequest {
    @NotNull
    private Long seqNum;

    @Size(max = 25)
    private String partyIdentificationNumber;
}
