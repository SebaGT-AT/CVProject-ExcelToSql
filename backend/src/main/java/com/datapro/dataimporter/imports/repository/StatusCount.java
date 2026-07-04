package com.datapro.dataimporter.imports.repository;

import com.datapro.dataimporter.imports.domain.ImportStatus;

public record StatusCount(
        ImportStatus status,
        long total
) {
}

