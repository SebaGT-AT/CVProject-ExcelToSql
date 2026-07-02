package com.datapro.dataimporter.security.dto;

public record UserSummaryResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}

