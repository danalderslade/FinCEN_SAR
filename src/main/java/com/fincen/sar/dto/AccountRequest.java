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
public class AccountRequest {
    @NotNull
    private Long seqNum;

    @NotBlank
    @Size(max = 40)
    private String accountNumberText;

    private Boolean accountClosedIndicator;
}
