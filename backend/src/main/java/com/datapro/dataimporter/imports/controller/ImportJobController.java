package com.datapro.dataimporter.imports.controller;

import com.datapro.dataimporter.common.dto.ApiResponse;
import com.datapro.dataimporter.common.dto.PagedResponse;
import com.datapro.dataimporter.imports.dto.CreateImportJobRequest;
import com.datapro.dataimporter.imports.dto.ImportJobDetailResponse;
import com.datapro.dataimporter.imports.dto.ImportJobSummaryResponse;
import com.datapro.dataimporter.imports.service.ImportJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/import-jobs")
@Tag(name = "Import Jobs", description = "Historial y trazabilidad de importaciones")
public class ImportJobController {

    private final ImportJobService importJobService;

    public ImportJobController(ImportJobService importJobService) {
        this.importJobService = importJobService;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Obtiene el historial paginado de importaciones")
    public ResponseEntity<ApiResponse<PagedResponse<ImportJobSummaryResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                importJobService.findAll(page, size),
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
}

