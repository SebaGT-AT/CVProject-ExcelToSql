package com.datapro.dataimporter.imports.dto;

public record DashboardErrorMetricResponse(
        String errorType,
        long total
) {
}

