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
public class AccountHoldingPartyRequest {
    @NotNull
    private Long seqNum;

    private Boolean nonUsFinancialInstitution;

    @Valid
    private AccountHoldingPartyIdentificationRequest identification;

    @Builder.Default
    @Valid
    private List<AccountRequest> accounts = new ArrayList<>();
}
