package com.datapro.dataimporter.security.service;

import com.datapro.dataimporter.security.dto.AuthRequest;
import com.datapro.dataimporter.security.dto.AuthResponse;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final ApplicationUserDetailsService userDetailsService;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            ApplicationUserDetailsService userDetailsService,
            AppUserRepository appUserRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        var appUser = appUserRepository.findByEmail(request.email()).orElseThrow();

        String token = jwtService.generateToken(userDetails, Map.of("role", appUser.getRole().name()));

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMillis() / 1000,
                appUser.getFullName(),
                appUser.getEmail(),
                appUser.getRole().name()
        );
    }
}

