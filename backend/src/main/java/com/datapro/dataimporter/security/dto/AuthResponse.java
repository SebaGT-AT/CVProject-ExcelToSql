package com.datapro.dataimporter.security.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String fullName,
        String email,
        String role
) {
}

