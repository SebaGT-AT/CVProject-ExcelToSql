package com.datapro.dataimporter.imports.controller;

import com.datapro.dataimporter.common.dto.ApiResponse;
import com.datapro.dataimporter.common.dto.PagedResponse;
import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportMode;
import com.datapro.dataimporter.imports.domain.ImportStatus;
import com.datapro.dataimporter.imports.domain.ReportFormat;
import com.datapro.dataimporter.imports.dto.CreateImportJobRequest;
import com.datapro.dataimporter.imports.dto.ImportJobFilterCriteria;
import com.datapro.dataimporter.imports.dto.ImportJobDetailResponse;
import com.datapro.dataimporter.imports.dto.ImportJobSummaryResponse;
import com.datapro.dataimporter.imports.service.ImportExecutionService;
import com.datapro.dataimporter.imports.service.ImportJobService;
import com.datapro.dataimporter.imports.service.ImportReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/import-jobs")
@Tag(name = "Import Jobs", description = "Historial y trazabilidad de importaciones")
public class ImportJobController {

    private final ImportJobService importJobService;
    private final ImportExecutionService importExecutionService;
    private final ImportReportService importReportService;

    public ImportJobController(
            ImportJobService importJobService,
            ImportExecutionService importExecutionService,
            ImportReportService importReportService
    ) {
        this.importJobService = importJobService;
        this.importExecutionService = importExecutionService;
        this.importReportService = importReportService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Registra una ejecucion de importacion")
    public ResponseEntity<ApiResponse<ImportJobDetailResponse>> create(
            @Valid @RequestBody CreateImportJobRequest request,
            Authentication authentication
    ) {
        var response = importJobService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Importacion registrada correctamente"));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Procesa un archivo CSV o XLSX y persiste los registros validos")
    public ResponseEntity<ApiResponse<ImportJobDetailResponse>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam ImportEntityType entityType,
            @RequestParam ImportMode importMode,
            Authentication authentication
    ) {
        var response = importExecutionService.execute(authentication.getName(), file, entityType, importMode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Archivo procesado correctamente"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Obtiene el historial paginado de importaciones")
    public ResponseEntity<ApiResponse<PagedResponse<ImportJobSummaryResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) ImportEntityType entityType,
            @RequestParam(required = false) ImportStatus status,
            @RequestParam(required = false) String initiatedByEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        var criteria = new ImportJobFilterCriteria(fileName, entityType, status, initiatedByEmail, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(
                importJobService.findAll(page, size, criteria),
                "Historial de importaciones obtenido correctamente"
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Obtiene el detalle de una importacion")
    public ResponseEntity<ApiResponse<ImportJobDetailResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                importJobService.findById(id),
                "Detalle de importacion obtenido correctamente"
        ));
    }

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Exporta el historial filtrado en CSV o PDF")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam ReportFormat format,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) ImportEntityType entityType,
            @RequestParam(required = false) ImportStatus status,
            @RequestParam(required = false) String initiatedByEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        var criteria = new ImportJobFilterCriteria(fileName, entityType, status, initiatedByEmail, dateFrom, dateTo);
        var report = importReportService.export(format, criteria);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + report.fileName())
                .contentType(MediaType.parseMediaType(report.contentType()))
                .body(report.content());
    }
}

