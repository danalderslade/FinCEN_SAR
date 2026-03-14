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
public class PartyOccupationRequest {
    @NotNull
    private Long seqNum;

    @Size(min = 3, max = 6)
    private String naicsCode;

    @Size(max = 50)
    private String occupationBusinessText;
}