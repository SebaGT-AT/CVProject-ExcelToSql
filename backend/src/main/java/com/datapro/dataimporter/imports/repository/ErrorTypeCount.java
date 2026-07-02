package com.datapro.dataimporter.imports.repository;

import com.datapro.dataimporter.imports.domain.ImportErrorType;

public record ErrorTypeCount(
        ImportErrorType errorType,
        long total
) {
}

