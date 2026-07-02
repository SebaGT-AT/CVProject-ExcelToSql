package com.datapro.dataimporter.imports.dto;

import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateImportErrorRequest(
        @Min(value = 1, message = "El numero de fila debe ser mayor a cero")
        int rowNumber,
        @Size(max = 120, message = "El nombre del campo no puede exceder 120 caracteres")
        String fieldName,
        @Size(max = 255, message = "El valor rechazado no puede exceder 255 caracteres")
        String rejectedValue,
        @NotNull(message = "El tipo de error es obligatorio")
        ImportErrorType errorType,
        @NotNull(message = "La severidad del error es obligatoria")
        ImportErrorSeverity severity,
        @NotBlank(message = "El mensaje del error es obligatorio")
        @Size(max = 255, message = "El mensaje del error no puede exceder 255 caracteres")
        String message
) {
}

