package com.datapro.dataimporter.imports.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long importsToday,
        long recordsProcessedToday,
        double successRate,
        List<DashboardErrorMetricResponse> topErrors,
        List<ImportJobSummaryResponse> recentImports
) {
}

