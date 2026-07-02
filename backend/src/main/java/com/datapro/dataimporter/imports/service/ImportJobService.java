package com.datapro.dataimporter.imports.service;

import com.datapro.dataimporter.common.dto.PagedResponse;
import com.datapro.dataimporter.common.exception.BusinessRuleException;
import com.datapro.dataimporter.common.exception.ResourceNotFoundException;
import com.datapro.dataimporter.imports.domain.ImportError;
import com.datapro.dataimporter.imports.domain.ImportStatus;
import com.datapro.dataimporter.imports.dto.CreateImportErrorRequest;
import com.datapro.dataimporter.imports.dto.CreateImportJobRequest;
import com.datapro.dataimporter.imports.dto.ImportErrorResponse;
import com.datapro.dataimporter.imports.dto.ImportJobDetailResponse;
import com.datapro.dataimporter.imports.dto.ImportJobSummaryResponse;
import com.datapro.dataimporter.imports.repository.ImportJobRepository;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ImportJobService {

    private final ImportJobRepository importJobRepository;
    private final AppUserRepository appUserRepository;

    public ImportJobService(ImportJobRepository importJobRepository, AppUserRepository appUserRepository) {
        this.importJobRepository = importJobRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public ImportJobDetailResponse create(String currentUserEmail, CreateImportJobRequest request) {
        validateBusinessRules(request);

        var user = appUserRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));

        OffsetDateTime finishedAt = request.finishedAt();
        if (isTerminalStatus(request.status()) && finishedAt == null) {
            finishedAt = OffsetDateTime.now();
        }

        var importJob = new com.datapro.dataimporter.imports.domain.ImportJob(
                request.fileName(),
                request.entityType(),
                request.fileType(),
                request.importMode(),
                request.status(),
                request.totalRecords(),
                request.successfulRecords(),
                request.failedRecords(),
                request.durationMs(),
                request.startedAt(),
                finishedAt,
                user
        );

        List<CreateImportErrorRequest> errors = request.errors() == null ? List.of() : request.errors();
        errors.stream()
                .map(this::toEntity)
                .forEach(importJob::addError);

        return toDetailResponse(importJobRepository.save(importJob));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ImportJobSummaryResponse> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var jobs = importJobRepository.findAll(pageable).map(this::toSummaryResponse);
        return PagedResponse.from(jobs);
    }

    @Transactional(readOnly = true)
    public ImportJobDetailResponse findById(Long id) {
        var job = importJobRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Importacion no encontrada con id " + id));

        return toDetailResponse(job);
    }

    @Transactional(readOnly = true)
    public List<ImportJobSummaryResponse> findRecent(int limit) {
        return importJobRepository.findRecentImports(PageRequest.of(0, limit))
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private void validateBusinessRules(CreateImportJobRequest request) {
        int total = request.totalRecords();
        int successful = request.successfulRecords();
        int failed = request.failedRecords();

        if (successful + failed > total) {
            throw new BusinessRuleException("La suma de registros exitosos y fallidos no puede superar el total");
        }

        int declaredErrors = request.errors() == null ? 0 : request.errors().size();
        if (declaredErrors > failed) {
            throw new BusinessRuleException("La cantidad de errores no puede superar la cantidad de registros fallidos");
        }

        if (isTerminalStatus(request.status()) && total > 0 && successful + failed != total) {
            throw new BusinessRuleException("Para estados finales, exitosos y fallidos deben cuadrar con el total");
        }

        if (request.finishedAt() != null && request.startedAt() != null && request.finishedAt().isBefore(request.startedAt())) {
            throw new BusinessRuleException("La fecha de finalizacion no puede ser anterior al inicio");
        }
    }

    private boolean isTerminalStatus(ImportStatus status) {
        return status == ImportStatus.COMPLETED
                || status == ImportStatus.PARTIALLY_COMPLETED
                || status == ImportStatus.FAILED
                || status == ImportStatus.CANCELLED;
    }

    private ImportError toEntity(CreateImportErrorRequest request) {
        return new ImportError(
                request.rowNumber(),
                request.fieldName(),
                request.rejectedValue(),
                request.errorType(),
                request.severity(),
                request.message()
        );
    }

    private ImportJobSummaryResponse toSummaryResponse(com.datapro.dataimporter.imports.domain.ImportJob job) {
        return new ImportJobSummaryResponse(
                job.getId(),
                job.getFileName(),
                job.getEntityType().name(),
                job.getFileType().name(),
                job.getImportMode().name(),
                job.getStatus().name(),
                job.getTotalRecords(),
                job.getSuccessfulRecords(),
                job.getFailedRecords(),
                job.getDurationMs(),
                calculateSuccessRate(job.getSuccessfulRecords(), job.getTotalRecords()),
                job.getInitiatedBy().getFullName(),
                job.getCreatedAt()
        );
    }

    private ImportJobDetailResponse toDetailResponse(com.datapro.dataimporter.imports.domain.ImportJob job) {
        return new ImportJobDetailResponse(
                job.getId(),
                job.getFileName(),
                job.getEntityType().name(),
                job.getFileType().name(),
                job.getImportMode().name(),
                job.getStatus().name(),
                job.getTotalRecords(),
                job.getSuccessfulRecords(),
                job.getFailedRecords(),
                job.getDurationMs(),
                calculateSuccessRate(job.getSuccessfulRecords(), job.getTotalRecords()),
                job.getInitiatedBy().getFullName(),
                job.getInitiatedBy().getEmail(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getCreatedAt(),
                job.getErrors().stream().map(this::toErrorResponse).toList()
        );
    }

    private ImportErrorResponse toErrorResponse(ImportError error) {
        return new ImportErrorResponse(
                error.getId(),
                error.getRowNumber(),
                error.getFieldName(),
                error.getRejectedValue(),
                error.getErrorType().name(),
                error.getSeverity().name(),
                error.getMessage()
        );
    }

    private double calculateSuccessRate(int successfulRecords, int totalRecords) {
        if (totalRecords == 0) {
            return 0.0;
        }

        return (successfulRecords * 100.0) / totalRecords;
    }
}

