package com.datapro.dataimporter.catalog.repository;

import com.datapro.dataimporter.catalog.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsBySupplierCode(String supplierCode);
    boolean existsByEmail(String email);
}

