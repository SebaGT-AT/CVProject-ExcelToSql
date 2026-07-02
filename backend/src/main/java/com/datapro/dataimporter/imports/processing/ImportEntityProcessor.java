package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportMode;

import java.util.List;
import java.util.Map;

public interface ImportEntityProcessor {

    ImportEntityType supports();

    ProcessingResult process(List<Map<String, String>> rows, ImportMode importMode);
}

