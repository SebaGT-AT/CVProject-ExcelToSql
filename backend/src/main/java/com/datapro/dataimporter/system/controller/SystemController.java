package com.datapro.dataimporter.system.controller;

import com.datapro.dataimporter.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "Endpoints de sistema")
public class SystemController {

    @GetMapping("/health")
    @Operation(summary = "Valida que la API este operativa")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "status", "UP",
                        "service", "Data Importer Pro API",
                        "timestamp", OffsetDateTime.now().toString()
                ),
                "Servicio operativo"
        ));
    }
}

