package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.imports.domain.ImportFileType;

import java.util.List;
import java.util.Map;

public record ParsedImportFile(
        ImportFileType fileType,
        List<Map<String, String>> rows
) {
}

