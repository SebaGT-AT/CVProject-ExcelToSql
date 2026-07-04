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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportReportControllerIntegrationTest {

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
    @WithMockUser(username = "admin@datapro.com", roles = {"ADMIN"})
    void shouldExportFilteredCsvReport() throws Exception {
        createImport("productos_export.csv", "PRODUCT", "COMPLETED", 2, 2, 0);
        createImport("clientes_export.xlsx", "CUSTOMER", "PARTIALLY_COMPLETED", 3, 2, 1);

        mockMvc.perform(get("/api/v1/import-jobs/report")
                        .param("format", "CSV")
                        .param("entityType", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=import-report.csv"))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(body).contains("clientes_export.xlsx");
                    org.assertj.core.api.Assertions.assertThat(body).doesNotContain("productos_export.csv");
                });
    }

    @Test
    @WithMockUser(username = "admin@datapro.com", roles = {"ADMIN"})
    void shouldExportPdfReport() throws Exception {
        createImport("empleados_export.xlsx", "EMPLOYEE", "COMPLETED", 2, 2, 0);

        mockMvc.perform(get("/api/v1/import-jobs/report")
                        .param("format", "PDF"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=import-report.pdf"))
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(result -> {
                    byte[] content = result.getResponse().getContentAsByteArray();
                    org.assertj.core.api.Assertions.assertThat(content).isNotEmpty();
                    org.assertj.core.api.Assertions.assertThat(new String(content, 0, 4)).isEqualTo("%PDF");
                });
    }

    private void createImport(
            String fileName,
            String entityType,
            String status,
            int totalRecords,
            int successfulRecords,
            int failedRecords
    ) throws Exception {
        var request = Map.of(
                "fileName", fileName,
                "entityType", entityType,
                "fileType", "XLSX",
                "importMode", "PARTIAL_ALLOWED",
                "status", status,
                "totalRecords", totalRecords,
                "successfulRecords", successfulRecords,
                "failedRecords", failedRecords,
                "durationMs", 1500,
                "errors", failedRecords == 0 ? List.of() : List.of(
                        Map.of(
                                "rowNumber", 2,
                                "fieldName", "email",
                                "rejectedValue", "bad-mail",
                                "errorType", "INVALID_EMAIL",
                                "severity", "ERROR",
                                "message", "Correo invalido"
                        )
                )
        );

        mockMvc.perform(post("/api/v1/import-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
