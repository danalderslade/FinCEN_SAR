package com.fincen.sar.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ActivitySupportDocumentRequest {
    @NotNull
    private Long seqNum;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = ".*\\.csv$", message = "fileName must end with .csv")
    private String originalAttachmentFileName;
}