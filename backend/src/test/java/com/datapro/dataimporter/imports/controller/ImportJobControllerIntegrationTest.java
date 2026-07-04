package com.datapro.dataimporter.imports.controller;

import com.datapro.dataimporter.imports.repository.ImportJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportJobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImportJobRepository importJobRepository;

    @BeforeEach
    void setUp() {
        importJobRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "operator@datapro.com", roles = {"OPERATOR"})
    void shouldCreateAndListImportJobs() throws Exception {
        var request = Map.of(
                "fileName", "clientes_julio.xlsx",
                "entityType", "CUSTOMER",
                "fileType", "XLSX",
                "importMode", "PARTIAL_ALLOWED",
                "status", "PARTIALLY_COMPLETED",
                "totalRecords", 3,
                "successfulRecords", 2,
                "failedRecords", 1,
                "durationMs", 1800,
                "errors", List.of(
                        Map.of(
                                "rowNumber", 2,
                                "fieldName", "email",
                                "rejectedValue", "correo-invalido",
                                "errorType", "INVALID_EMAIL",
                                "severity", "ERROR",
                                "message", "El correo no cumple el formato esperado"
                        )
                )
        );

        mockMvc.perform(post("/api/v1/import-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("clientes_julio.xlsx"))
                .andExpect(jsonPath("$.data.failedRecords").value(1))
                .andExpect(jsonPath("$.data.errors[0].errorType").value("INVALID_EMAIL"));

        mockMvc.perform(get("/api/v1/import-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fileName").value("clientes_julio.xlsx"))
                .andExpect(jsonPath("$.data.content[0].initiatedByName").value("Operator User"));

        mockMvc.perform(get("/api/v1/import-jobs")
                        .param("entityType", "CUSTOMER")
                        .param("status", "PARTIALLY_COMPLETED")
                        .param("fileName", "clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].entityType").value("CUSTOMER"));
    }

    @Test
    @WithMockUser(username = "admin@datapro.com", roles = {"ADMIN"})
    void shouldReturnImportJobDetail() throws Exception {
        var request = Map.of(
                "fileName", "inventario_sucursal_a.csv",
                "entityType", "INVENTORY",
                "fileType", "CSV",
                "importMode", "FULL",
                "status", "COMPLETED",
                "totalRecords", 2,
                "successfulRecords", 2,
                "failedRecords", 0,
                "durationMs", 900,
                "errors", List.of()
        );

        String responseBody = mockMvc.perform(post("/api/v1/import-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long importId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/import-jobs/{id}", importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(importId))
                .andExpect(jsonPath("$.data.entityType").value("INVENTORY"))
                .andExpect(jsonPath("$.data.successRate").value(100.0));
    }
}

