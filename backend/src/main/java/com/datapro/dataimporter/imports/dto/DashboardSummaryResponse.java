package com.datapro.dataimporter.imports.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long importsToday,
        long recordsProcessedToday,
        double successRate,
        long averageDurationMs,
        List<DashboardErrorMetricResponse> topErrors,
        List<DashboardStatusMetricResponse> importsByStatus,
        List<DashboardEntityMetricResponse> importsByEntityType,
        List<ImportJobSummaryResponse> recentImports
) {
}

