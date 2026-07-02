package com.datapro.dataimporter.imports.service;

import com.datapro.dataimporter.common.exception.BusinessRuleException;
import com.datapro.dataimporter.common.exception.ResourceNotFoundException;
import com.datapro.dataimporter.imports.domain.ImportError;
import com.datapro.dataimporter.imports.domain.ImportJob;
import com.datapro.dataimporter.imports.domain.ImportMode;
import com.datapro.dataimporter.imports.dto.ImportJobDetailResponse;
import com.datapro.dataimporter.imports.processing.ImportEntityProcessor;
import com.datapro.dataimporter.imports.processing.ImportFileReader;
import com.datapro.dataimporter.imports.repository.ImportJobRepository;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportExecutionService {

    private final ImportFileReader importFileReader;
    private final AppUserRepository appUserRepository;
    private final ImportJobRepository importJobRepository;
    private final ImportJobService importJobService;
    private final Map<com.datapro.dataimporter.imports.domain.ImportEntityType, ImportEntityProcessor> processors;

    public ImportExecutionService(
            ImportFileReader importFileReader,
            AppUserRepository appUserRepository,
            ImportJobRepository importJobRepository,
            ImportJobService importJobService,
            List<ImportEntityProcessor> processors
    ) {
        this.importFileReader = importFileReader;
        this.appUserRepository = appUserRepository;
        this.importJobRepository = importJobRepository;
        this.importJobService = importJobService;
        this.processors = new EnumMap<>(com.datapro.dataimporter.imports.domain.ImportEntityType.class);
        processors.forEach(processor -> this.processors.put(processor.supports(), processor));
    }

    @Transactional
    public ImportJobDetailResponse execute(
            String currentUserEmail,
            MultipartFile file,
            com.datapro.dataimporter.imports.domain.ImportEntityType entityType,
            ImportMode importMode
    ) {
        var user = appUserRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));

        ImportEntityProcessor processor = processors.get(entityType);
        if (processor == null) {
            throw new BusinessRuleException("No existe un procesador configurado para " + entityType.name());
        }

        OffsetDateTime startedAt = OffsetDateTime.now();
        var parsedFile = importFileReader.read(file);
        var result = processor.process(parsedFile.rows(), importMode);
        OffsetDateTime finishedAt = OffsetDateTime.now();

        ImportJob job = new ImportJob(
                file.getOriginalFilename(),
                entityType,
                parsedFile.fileType(),
                importMode,
                result.status(),
                result.totalRecords(),
                result.successfulRecords(),
                result.failedRecords(),
                java.time.Duration.between(startedAt, finishedAt).toMillis(),
                startedAt,
                finishedAt,
                user
        );

        result.errors().stream()
                .map(error -> new ImportError(
                        Math.max(error.rowNumber(), 1),
                        error.fieldName(),
                        error.rejectedValue(),
                        error.errorType(),
                        error.severity(),
                        error.message()
                ))
                .forEach(job::addError);

        ImportJob saved = importJobRepository.save(job);
        return importJobService.toDetailResponse(saved);
    }
}
