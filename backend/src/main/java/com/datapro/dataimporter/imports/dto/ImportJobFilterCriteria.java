package com.datapro.dataimporter.imports.dto;

import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportStatus;

import java.time.LocalDate;

public record ImportJobFilterCriteria(
        String fileName,
        ImportEntityType entityType,
        ImportStatus status,
        String initiatedByEmail,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

