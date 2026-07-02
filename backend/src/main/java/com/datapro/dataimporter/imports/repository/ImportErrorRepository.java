package com.datapro.dataimporter.imports.repository;

import com.datapro.dataimporter.imports.domain.ImportError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {
}

