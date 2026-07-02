package com.datapro.dataimporter.imports.service;

import com.datapro.dataimporter.common.exception.BusinessRuleException;
import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportFileType;
import com.datapro.dataimporter.imports.domain.ImportMode;
import com.datapro.dataimporter.imports.domain.ImportStatus;
import com.datapro.dataimporter.imports.dto.CreateImportJobRequest;
import com.datapro.dataimporter.imports.repository.ImportJobRepository;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ImportJobServiceTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ImportJobService importJobService;

    @Test
    void shouldRejectInconsistentTotalsForTerminalStatus() {
        var request = new CreateImportJobRequest(
                "clientes.xlsx",
                ImportEntityType.CUSTOMER,
                ImportFileType.XLSX,
                ImportMode.PARTIAL_ALLOWED,
                ImportStatus.COMPLETED,
                10,
                8,
                1,
                1200,
                null,
                null,
                List.of()
        );

        assertThatThrownBy(() -> importJobService.create("operator@datapro.com", request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cuadrar con el total");
    }
}
