package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.imports.domain.ImportStatus;

import java.util.List;

public record ProcessingResult(
        ImportStatus status,
        int totalRecords,
        int successfulRecords,
        int failedRecords,
        List<ProcessingError> errors
) {
}

