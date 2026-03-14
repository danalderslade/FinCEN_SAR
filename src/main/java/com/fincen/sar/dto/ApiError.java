package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    private OffsetDateTime timestamp;
}
