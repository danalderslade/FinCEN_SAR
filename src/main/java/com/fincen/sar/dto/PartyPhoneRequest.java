package com.fincen.sar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyPhoneRequest {
    @NotNull
    private Long seqNum;

    @Size(max = 16)
    private String phoneNumberText;

    @Size(max = 6)
    private String phoneNumberExtension;

    @Pattern(regexp = "^[RWMF]$")
    private String phoneNumberTypeCode;
}