package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.catalog.domain.Employee;
import com.datapro.dataimporter.catalog.repository.EmployeeRepository;
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
public class EmployeeImportProcessor extends AbstractImportProcessor<Employee> {

    private static final Set<String> EXPECTED_HEADERS = Set.of("employee_code", "full_name", "email", "phone", "hire_date");

    private final EmployeeRepository employeeRepository;

    public EmployeeImportProcessor(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected Set<String> expectedHeaders() {
        return EXPECTED_HEADERS;
    }

    @Override
    protected Optional<Employee> parseRow(int rowNumber, Map<String, String> row, List<ProcessingError> errors, Set<String> seenKeys) {
        String employeeCode = requiredValue(rowNumber, row, "employee_code", errors);
        String fullName = requiredValue(rowNumber, row, "full_name", errors);
        String email = requiredValue(rowNumber, row, "email", errors);
        String phone = requiredValue(rowNumber, row, "phone", errors);
        String hireDateValue = requiredValue(rowNumber, row, "hire_date", errors);

        if (employeeCode != null) {
            registerDuplicateIfNeeded(rowNumber, "employee_code", employeeCode, seenKeys, errors);
            if (employeeRepository.existsByEmployeeCode(employeeCode)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "employee_code",
                        employeeCode,
                        ImportErrorType.DUPLICATE_VALUE,
                        ImportErrorSeverity.CRITICAL,
                        "Ya existe un empleado con ese codigo"
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
            if (employeeRepository.existsByEmail(email)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "email",
                        email,
                        ImportErrorType.DUPLICATE_VALUE,
                        ImportErrorSeverity.CRITICAL,
                        "Ya existe un empleado con ese correo"
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

        LocalDate hireDate = hireDateValue == null ? null : parseDate(rowNumber, "hire_date", hireDateValue, errors);

        if (!errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Employee(employeeCode, fullName, email, phone, hireDate));
    }

    @Override
    protected void persistValidRecords(List<Employee> validRecords) {
        employeeRepository.saveAll(validRecords);
    }

    @Override
    protected ImportEntityType entityType() {
        return ImportEntityType.EMPLOYEE;
    }
}

