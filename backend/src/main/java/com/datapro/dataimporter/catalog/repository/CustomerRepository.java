package com.datapro.dataimporter.catalog.repository;

import com.datapro.dataimporter.catalog.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCustomerCode(String customerCode);
    boolean existsByEmail(String email);
}

