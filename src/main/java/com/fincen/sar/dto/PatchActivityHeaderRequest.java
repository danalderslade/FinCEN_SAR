package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchActivityHeaderRequest {
    private LocalDate filingDate;

    @Size(min = 14, max = 14)
    private String efilingPriorDocumentNumber;

    @Size(max = 50)
    private String filingInstitutionNoteToFincen;
}
