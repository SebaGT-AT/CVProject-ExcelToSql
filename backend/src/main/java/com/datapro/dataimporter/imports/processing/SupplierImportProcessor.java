package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.catalog.domain.Supplier;
import com.datapro.dataimporter.catalog.repository.SupplierRepository;
import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;
import com.datapro.dataimporter.imports.processing.Support.AbstractImportProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class SupplierImportProcessor extends AbstractImportProcessor<Supplier> {

    private static final Set<String> EXPECTED_HEADERS = Set.of("supplier_code", "company_name", "email", "phone");

    private final SupplierRepository supplierRepository;

    public SupplierImportProcessor(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    protected Set<String> expectedHeaders() {
        return EXPECTED_HEADERS;
    }

    @Override
    protected Optional<Supplier> parseRow(int rowNumber, Map<String, String> row, List<ProcessingError> errors, Set<String> seenKeys) {
        String supplierCode = requiredValue(rowNumber, row, "supplier_code", errors);
        String companyName = requiredValue(rowNumber, row, "company_name", errors);
        String email = requiredValue(rowNumber, row, "email", errors);
        String phone = requiredValue(rowNumber, row, "phone", errors);

        if (supplierCode != null) {
            registerDuplicateIfNeeded(rowNumber, "supplier_code", supplierCode, seenKeys, errors);
            if (supplierRepository.existsBySupplierCode(supplierCode)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "supplier_code",
                        supplierCode,
                        ImportErrorType.DUPLICATE_VALUE,
                        ImportErrorSeverity.CRITICAL,
                        "Ya existe un proveedor con ese codigo"
                ));
            }
        }

        if (email != null) {
            if (!isValidEmail(email)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "email",
                        email,
                        ImportErrorType.INVALID_EMAIL,
                        ImportErrorSeverity.ERROR,
                        "El correo no tiene un formato valido"
                ));
            }
            if (supplierRepository.existsByEmail(email)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "email",
                        email,
                        ImportErrorType.DUPLICATE_VALUE,
                        ImportErrorSeverity.CRITICAL,
                        "Ya existe un proveedor con ese correo"
                ));
            }
        }

        if (phone != null && !isValidPhone(phone)) {
            errors.add(new ProcessingError(
                    rowNumber,
                    "phone",
                    phone,
                    ImportErrorType.INVALID_PHONE,
                    ImportErrorSeverity.ERROR,
                    "El telefono no tiene un formato valido"
            ));
        }

        if (!errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Supplier(supplierCode, companyName, email, phone));
    }

    @Override
    protected void persistValidRecords(List<Supplier> validRecords) {
        supplierRepository.saveAll(validRecords);
    }

    @Override
    protected ImportEntityType entityType() {
        return ImportEntityType.SUPPLIER;
    }
}

