package com.datapro.dataimporter.imports.domain;

public enum ImportErrorType {
    REQUIRED_FIELD,
    INVALID_FORMAT,
    INVALID_EMAIL,
    INVALID_PHONE,
    INVALID_NUMBER,
    INVALID_DATE,
    DUPLICATE_VALUE,
    FOREIGN_KEY_NOT_FOUND,
    BUSINESS_RULE
}

