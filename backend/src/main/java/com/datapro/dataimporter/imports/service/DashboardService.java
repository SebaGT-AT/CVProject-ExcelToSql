package com.datapro.dataimporter.imports.service;

import com.datapro.dataimporter.imports.dto.DashboardErrorMetricResponse;
import com.datapro.dataimporter.imports.dto.DashboardSummaryResponse;
import com.datapro.dataimporter.imports.repository.ImportJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class DashboardService {

    private final ImportJobRepository importJobRepository;
    private final ImportJobService importJobService;

    public DashboardService(ImportJobRepository importJobRepository, ImportJobService importJobService) {
        this.importJobRepository = importJobRepository;
        this.importJobService = importJobService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfDay = now.toLocalDate().atStartOfDay(now.getOffset()).toOffsetDateTime();

        long importsToday = importJobRepository.countCreatedBetween(startOfDay, now);
        long recordsProcessedToday = importJobRepository.sumTotalRecordsCreatedBetween(startOfDay, now);
        long successfulRecordsToday = importJobRepository.sumSuccessfulRecordsCreatedBetween(startOfDay, now);

        double successRate = recordsProcessedToday == 0
                ? 0.0
                : (successfulRecordsToday * 100.0) / recordsProcessedToday;

        var topErrors = importJobRepository.findTopErrorTypes(org.springframework.data.domain.PageRequest.of(0, 5))
                .stream()
                .map(item -> new DashboardErrorMetricResponse(item.errorType().name(), item.total()))
                .toList();

        return new DashboardSummaryResponse(
                importsToday,
                recordsProcessedToday,
                successRate,
                topErrors,
                importJobService.findRecent(5)
        );
    }
}

