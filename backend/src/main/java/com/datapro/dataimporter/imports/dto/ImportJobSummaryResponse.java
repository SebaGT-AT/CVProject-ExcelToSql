package com.datapro.dataimporter.imports.dto;

import java.time.OffsetDateTime;

public record ImportJobSummaryResponse(
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
        OffsetDateTime createdAt
) {
}

