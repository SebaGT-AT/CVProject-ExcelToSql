package com.datapro.dataimporter.imports.dto;

public record ImportErrorResponse(
        Long id,
        int rowNumber,
        String fieldName,
        String rejectedValue,
        String errorType,
        String severity,
        String message
) {
}

