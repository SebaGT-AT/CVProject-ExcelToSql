package com.datapro.dataimporter.imports.controller;

import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportError;
import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;
import com.datapro.dataimporter.imports.domain.ImportFileType;
import com.datapro.dataimporter.imports.domain.ImportJob;
import com.datapro.dataimporter.imports.domain.ImportMode;
import com.datapro.dataimporter.imports.domain.ImportStatus;
import com.datapro.dataimporter.imports.repository.ImportJobRepository;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        importJobRepository.deleteAll();

        var admin = appUserRepository.findByEmail("admin@datapro.com").orElseThrow();

        var importJob = new ImportJob(
                "proveedores_julio.csv",
                ImportEntityType.SUPPLIER,
                ImportFileType.CSV,
                ImportMode.PARTIAL_ALLOWED,
                ImportStatus.PARTIALLY_COMPLETED,
                5,
                3,
                2,
                1500,
                OffsetDateTime.now().minusMinutes(3),
                OffsetDateTime.now().minusMinutes(2),
                admin
        );
        importJob.addError(new ImportError(
                3,
                "email",
                "correo-invalido",
                ImportErrorType.INVALID_EMAIL,
                ImportErrorSeverity.ERROR,
                "Correo invalido"
        ));
        importJob.addError(new ImportError(
                4,
                "supplierCode",
                "SUP-001",
                ImportErrorType.DUPLICATE_VALUE,
                ImportErrorSeverity.ERROR,
                "Codigo duplicado"
        ));

        importJobRepository.save(importJob);
    }

    @Test
    @WithMockUser(username = "admin@datapro.com", roles = {"ADMIN"})
    void shouldReturnDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.importsToday").value(1))
                .andExpect(jsonPath("$.data.recordsProcessedToday").value(5))
                .andExpect(jsonPath("$.data.successRate").value(60.0))
                .andExpect(jsonPath("$.data.topErrors[0].total").value(1))
                .andExpect(jsonPath("$.data.recentImports[0].fileName").value("proveedores_julio.csv"));
    }
}
