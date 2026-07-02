package com.datapro.dataimporter.security.controller;

import com.datapro.dataimporter.common.dto.ApiResponse;
import com.datapro.dataimporter.security.dto.UserSummaryResponse;
import com.datapro.dataimporter.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Administracion de usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtiene la lista de usuarios")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.findAll(), "Usuarios obtenidos correctamente"));
    }
}

