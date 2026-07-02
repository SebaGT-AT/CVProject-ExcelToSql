package com.datapro.dataimporter.imports.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ImportJobDetailResponse(
        Long id,
        String fileName,
        String entityType,
        String fileType,
        String importMode,
        String status,
        int totalRecords,
        int successfulRecords,
        int failedRecords,
        long durationMs,
        double successRate,
        String initiatedByName,
        String initiatedByEmail,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        OffsetDateTime createdAt,
        List<ImportErrorResponse> errors
) {
}

