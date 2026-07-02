package com.datapro.dataimporter.imports.domain;

import com.datapro.dataimporter.common.domain.AuditableEntity;
import com.datapro.dataimporter.security.domain.AppUser;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "import_jobs",
        indexes = {
                @Index(name = "idx_import_job_created_at", columnList = "created_at"),
                @Index(name = "idx_import_job_status", columnList = "status"),
                @Index(name = "idx_import_job_entity_type", columnList = "entity_type")
        }
)
public class ImportJob extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 180)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private ImportEntityType entityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private ImportFileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_mode", nullable = false, length = 30)
    private ImportMode importMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ImportStatus status;

    @Column(name = "total_records", nullable = false)
    private int totalRecords;

    @Column(name = "successful_records", nullable = false)
    private int successfulRecords;

    @Column(name = "failed_records", nullable = false)
    private int failedRecords;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiated_by_user_id", nullable = false)
    private AppUser initiatedBy;

    @OneToMany(mappedBy = "importJob", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rowNumber ASC, id ASC")
    private final List<ImportError> errors = new ArrayList<>();

    protected ImportJob() {
    }

    public ImportJob(
            String fileName,
            ImportEntityType entityType,
            ImportFileType fileType,
            ImportMode importMode,
            ImportStatus status,
            int totalRecords,
            int successfulRecords,
            int failedRecords,
            long durationMs,
            OffsetDateTime startedAt,
            OffsetDateTime finishedAt,
            AppUser initiatedBy
    ) {
        this.fileName = fileName;
        this.entityType = entityType;
        this.fileType = fileType;
        this.importMode = importMode;
        this.status = status;
        this.totalRecords = totalRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.durationMs = durationMs;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.initiatedBy = initiatedBy;
    }

    public void addError(ImportError error) {
        errors.add(error);
        error.assignImportJob(this);
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public ImportEntityType getEntityType() {
        return entityType;
    }

    public ImportFileType getFileType() {
        return fileType;
    }

    public ImportMode getImportMode() {
        return importMode;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getSuccessfulRecords() {
        return successfulRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public AppUser getInitiatedBy() {
        return initiatedBy;
    }

    public List<ImportError> getErrors() {
        return List.copyOf(errors);
    }
}
