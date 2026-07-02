package com.datapro.dataimporter.imports.dto;

import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportFileType;
import com.datapro.dataimporter.imports.domain.ImportMode;
import com.datapro.dataimporter.imports.domain.ImportStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateImportJobRequest(
        @NotBlank(message = "El nombre del archivo es obligatorio")
        @Size(max = 180, message = "El nombre del archivo no puede exceder 180 caracteres")
        String fileName,
        @NotNull(message = "El tipo de entidad es obligatorio")
        ImportEntityType entityType,
        @NotNull(message = "El tipo de archivo es obligatorio")
        ImportFileType fileType,
        @NotNull(message = "El modo de importacion es obligatorio")
        ImportMode importMode,
        @NotNull(message = "El estado de la importacion es obligatorio")
        ImportStatus status,
        @Min(value = 0, message = "El total de registros no puede ser negativo")
        int totalRecords,
        @Min(value = 0, message = "La cantidad de registros exitosos no puede ser negativa")
        int successfulRecords,
        @Min(value = 0, message = "La cantidad de registros fallidos no puede ser negativa")
        int failedRecords,
        @Min(value = 0, message = "La duracion no puede ser negativa")
        long durationMs,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        @Valid
        List<CreateImportErrorRequest> errors
) {
}

