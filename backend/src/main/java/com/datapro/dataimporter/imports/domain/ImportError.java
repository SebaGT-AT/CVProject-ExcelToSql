package com.datapro.dataimporter.imports.domain;

import com.datapro.dataimporter.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "import_errors",
        indexes = {
                @Index(name = "idx_import_error_type", columnList = "error_type"),
                @Index(name = "idx_import_error_job", columnList = "import_job_id")
        }
)
public class ImportError extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "field_name", length = 120)
    private String fieldName;

    @Column(name = "rejected_value", length = 255)
    private String rejectedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", nullable = false, length = 40)
    private ImportErrorType errorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImportErrorSeverity severity;

    @Column(nullable = false, length = 255)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_job_id", nullable = false)
    private ImportJob importJob;

    protected ImportError() {
    }

    public ImportError(
            int rowNumber,
            String fieldName,
            String rejectedValue,
            ImportErrorType errorType,
            ImportErrorSeverity severity,
            String message
    ) {
        this.rowNumber = rowNumber;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
        this.errorType = errorType;
        this.severity = severity;
        this.message = message;
    }

    void assignImportJob(ImportJob importJob) {
        this.importJob = importJob;
    }

    public Long getId() {
        return id;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }

    public ImportErrorType getErrorType() {
        return errorType;
    }

    public ImportErrorSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}
