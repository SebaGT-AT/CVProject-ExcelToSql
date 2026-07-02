package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.catalog.domain.Customer;
import com.datapro.dataimporter.catalog.repository.CustomerRepository;
import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;
import com.datapro.dataimporter.imports.processing.Support.AbstractImportProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomerImportProcessor extends AbstractImportProcessor<Customer> {

    private static final Set<String> EXPECTED_HEADERS = Set.of(
            "customer_code",
            "full_name",
            "email",
            "phone",
            "registration_date"
    );

    private final CustomerRepository customerRepository;

    public CustomerImportProcessor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    protected Set<String> expectedHeaders() {
        return EXPECTED_HEADERS;
    }

    @Override
    protected Optional<Customer> parseRow(int rowNumber, Map<String, String> row, List<ProcessingError> errors, Set<String> seenKeys) {
        String customerCode = requiredValue(rowNumber, row, "customer_code", errors);
        String fullName = requiredValue(rowNumber, row, "full_name", errors);
        String email = requiredValue(rowNumber, row, "email", errors);
        String phone = requiredValue(rowNumber, row, "phone", errors);
        String registrationDateValue = requiredValue(rowNumber, row, "registration_date", errors);

        if (customerCode != null) {
            registerDuplicateIfNeeded(rowNumber, "customer_code", customerCode, seenKeys, errors);
            if (customerRepository.existsByCustomerCode(customerCode)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "customer_code",
                        customerCode,
                        ImportErrorType.DUPLICATE_VALUE,
                        ImportErrorSeverity.CRITICAL,
                        "Ya existe un cliente con ese codigo"
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
            if (customerRepository.existsByEmail(email)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "email",
                        email,
                        ImportErrorType.DUPLICATE_VALUE,
                        ImportErrorSeverity.CRITICAL,
                        "Ya existe un cliente con ese correo"
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

        LocalDate registrationDate = registrationDateValue == null
                ? null
                : parseDate(rowNumber, "registration_date", registrationDateValue, errors);

        if (!errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Customer(customerCode, fullName, email, phone, registrationDate));
    }

    @Override
    protected void persistValidRecords(List<Customer> validRecords) {
        customerRepository.saveAll(validRecords);
    }

    @Override
    protected ImportEntityType entityType() {
        return ImportEntityType.CUSTOMER;
    }
}

