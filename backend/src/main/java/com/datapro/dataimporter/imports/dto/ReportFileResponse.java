package com.datapro.dataimporter.imports.dto;

public record ReportFileResponse(
        byte[] content,
        String fileName,
        String contentType
) {
}

