package com.datapro.dataimporter.imports.processing.Support;

import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;
import com.datapro.dataimporter.imports.domain.ImportMode;
import com.datapro.dataimporter.imports.domain.ImportStatus;
import com.datapro.dataimporter.imports.processing.ImportEntityProcessor;
import com.datapro.dataimporter.imports.processing.ProcessingError;
import com.datapro.dataimporter.imports.processing.ProcessingResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractImportProcessor<T> implements ImportEntityProcessor {

    @Override
    public ProcessingResult process(List<Map<String, String>> rows, ImportMode importMode) {
        List<ProcessingError> errors = new ArrayList<>();
        List<T> validRecords = new ArrayList<>();

        Set<String> seenKeys = new HashSet<>();

        for (int index = 0; index < rows.size(); index++) {
            int rowNumber = index + 2;
            Map<String, String> row = rows.get(index);
            List<ProcessingError> rowErrors = new ArrayList<>();

            validateExpectedHeaders(row, rowErrors);

            Optional<T> parsedRecord = parseRow(rowNumber, row, rowErrors, seenKeys);
            if (rowErrors.isEmpty() && parsedRecord.isPresent()) {
                validRecords.add(parsedRecord.get());
            }

            errors.addAll(rowErrors);
        }

        boolean hasCriticalErrors = errors.stream().anyMatch(error -> error.severity() == ImportErrorSeverity.CRITICAL);

        if (importMode == ImportMode.FULL && !errors.isEmpty()) {
            errors.add(new ProcessingError(
                    1,
                    "importMode",
                    importMode.name(),
                    ImportErrorType.BUSINESS_RULE,
                    ImportErrorSeverity.CRITICAL,
                    "La importacion completa fue cancelada porque existen filas invalidas"
            ));

            return new ProcessingResult(ImportStatus.FAILED, rows.size(), 0, rows.size(), errors);
        }

        if (importMode == ImportMode.CANCEL_ON_CRITICAL && hasCriticalErrors) {
            errors.add(new ProcessingError(
                    1,
                    "importMode",
                    importMode.name(),
                    ImportErrorType.BUSINESS_RULE,
                    ImportErrorSeverity.CRITICAL,
                    "La importacion fue cancelada por errores criticos"
            ));

            return new ProcessingResult(ImportStatus.CANCELLED, rows.size(), 0, rows.size(), errors);
        }

        persistValidRecords(validRecords);

        int successfulRecords = validRecords.size();
        int failedRecords = rows.size() - successfulRecords;
        ImportStatus status = resolveStatus(rows.size(), successfulRecords, failedRecords);

        return new ProcessingResult(status, rows.size(), successfulRecords, failedRecords, errors);
    }

    protected abstract Set<String> expectedHeaders();

    protected abstract Optional<T> parseRow(
            int rowNumber,
            Map<String, String> row,
            List<ProcessingError> errors,
            Set<String> seenKeys
    );

    protected abstract void persistValidRecords(List<T> validRecords);

    protected abstract ImportEntityType entityType();

    @Override
    public ImportEntityType supports() {
        return entityType();
    }

    protected void validateExpectedHeaders(Map<String, String> row, List<ProcessingError> errors) {
        for (String header : expectedHeaders()) {
            if (!row.containsKey(header)) {
                errors.add(new ProcessingError(
                        1,
                        header,
                        null,
                        ImportErrorType.REQUIRED_FIELD,
                        ImportErrorSeverity.CRITICAL,
                        "Falta la columna requerida: " + header
                ));
            }
        }
    }

    protected String requiredValue(
            int rowNumber,
            Map<String, String> row,
            String header,
            List<ProcessingError> errors
    ) {
        String value = row.get(header);
        if (value == null || value.isBlank()) {
            errors.add(new ProcessingError(
                    rowNumber,
                    header,
                    value,
                    ImportErrorType.REQUIRED_FIELD,
                    ImportErrorSeverity.ERROR,
                    "El campo es obligatorio"
            ));
            return null;
        }
        return value.trim();
    }

    protected LocalDate parseDate(
            int rowNumber,
            String fieldName,
            String value,
            List<ProcessingError> errors
    ) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            errors.add(new ProcessingError(
                    rowNumber,
                    fieldName,
                    value,
                    ImportErrorType.INVALID_DATE,
                    ImportErrorSeverity.ERROR,
                    "La fecha debe usar el formato yyyy-MM-dd"
            ));
            return null;
        }
    }

    protected Integer parseInteger(
            int rowNumber,
            String fieldName,
            String value,
            List<ProcessingError> errors
    ) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            errors.add(new ProcessingError(
                    rowNumber,
                    fieldName,
                    value,
                    ImportErrorType.INVALID_NUMBER,
                    ImportErrorSeverity.ERROR,
                    "El valor debe ser numerico"
            ));
            return null;
        }
    }

    protected java.math.BigDecimal parseDecimal(
            int rowNumber,
            String fieldName,
            String value,
            List<ProcessingError> errors
    ) {
        try {
            return new java.math.BigDecimal(value);
        } catch (NumberFormatException ex) {
            errors.add(new ProcessingError(
                    rowNumber,
                    fieldName,
                    value,
                    ImportErrorType.INVALID_NUMBER,
                    ImportErrorSeverity.ERROR,
                    "El valor debe ser decimal"
            ));
            return null;
        }
    }

    protected boolean isValidEmail(String value) {
        return value != null && value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    protected boolean isValidPhone(String value) {
        return value != null && value.matches("^[+]?[0-9()\\-\\s]{7,20}$");
    }

    protected void registerDuplicateIfNeeded(
            int rowNumber,
            String fieldName,
            String value,
            Set<String> seenKeys,
            List<ProcessingError> errors
    ) {
        if (!seenKeys.add(value)) {
            errors.add(new ProcessingError(
                    rowNumber,
                    fieldName,
                    value,
                    ImportErrorType.DUPLICATE_VALUE,
                    ImportErrorSeverity.CRITICAL,
                    "El valor esta duplicado dentro del archivo"
            ));
        }
    }

    private ImportStatus resolveStatus(int totalRecords, int successfulRecords, int failedRecords) {
        if (totalRecords == 0) {
            return ImportStatus.COMPLETED;
        }
        if (successfulRecords == totalRecords) {
            return ImportStatus.COMPLETED;
        }
        if (successfulRecords == 0 && failedRecords > 0) {
            return ImportStatus.FAILED;
        }
        return ImportStatus.PARTIALLY_COMPLETED;
    }
}

