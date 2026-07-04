package com.datapro.dataimporter.imports.repository;

import com.datapro.dataimporter.imports.domain.ImportEntityType;

public record EntityTypeCount(
        ImportEntityType entityType,
        long total
) {
}

