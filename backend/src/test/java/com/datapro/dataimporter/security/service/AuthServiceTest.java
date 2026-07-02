package com.datapro.dataimporter.security.service;

import com.datapro.dataimporter.security.domain.AppUser;
import com.datapro.dataimporter.security.domain.Role;
import com.datapro.dataimporter.security.dto.AuthRequest;
import com.datapro.dataimporter.security.dto.AuthResponse;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ApplicationUserDetailsService userDetailsService;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("admin@datapro.com", "Admin123!");
    }

    @Test
    void shouldAuthenticateAndReturnJwtResponse() {
        var user = new User("admin@datapro.com", "encoded", List.of());
        var appUser = new AppUser("Administrator", "admin@datapro.com", "encoded", Role.ROLE_ADMIN, true);

        when(userDetailsService.loadUserByUsername(authRequest.email())).thenReturn(user);
        when(appUserRepository.findByEmail(authRequest.email())).thenReturn(Optional.of(appUser));
        when(jwtService.generateToken(eq(user), any(Map.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMillis()).thenReturn(86400000L);

        AuthResponse response = authService.authenticate(authRequest);

        verify(authenticationManager).authenticate(any());
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("admin@datapro.com");
        assertThat(response.role()).isEqualTo("ROLE_ADMIN");
    }
}

