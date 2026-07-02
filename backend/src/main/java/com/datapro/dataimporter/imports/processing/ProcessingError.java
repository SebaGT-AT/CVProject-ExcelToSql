package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;

public record ProcessingError(
        int rowNumber,
        String fieldName,
        String rejectedValue,
        ImportErrorType errorType,
        ImportErrorSeverity severity,
        String message
) {
}

