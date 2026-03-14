package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchSupportDocumentRequest {
    @Pattern(regexp = ".*\\.csv$", message = "fileName must end with .csv")
    private String originalAttachmentFileName;
}
