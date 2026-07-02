package com.datapro.dataimporter.catalog.repository;

import com.datapro.dataimporter.catalog.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
}

