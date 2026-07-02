package com.datapro.dataimporter.imports.domain;

public enum ImportStatus {
    RECEIVED,
    VALIDATING,
    PROCESSING,
    COMPLETED,
    PARTIALLY_COMPLETED,
    FAILED,
    CANCELLED
}

