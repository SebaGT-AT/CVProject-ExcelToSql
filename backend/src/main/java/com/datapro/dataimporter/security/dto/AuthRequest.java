package com.datapro.dataimporter.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato valido")
        String email,
        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {
}

