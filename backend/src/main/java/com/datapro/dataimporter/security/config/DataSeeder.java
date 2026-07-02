package com.datapro.dataimporter.security.config;

import com.datapro.dataimporter.security.domain.AppUser;
import com.datapro.dataimporter.security.domain.Role;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!appUserRepository.existsByEmail("admin@datapro.com")) {
            appUserRepository.save(new AppUser(
                    "Administrator",
                    "admin@datapro.com",
                    passwordEncoder.encode("Admin123!"),
                    Role.ROLE_ADMIN,
                    true
            ));
        }

        if (!appUserRepository.existsByEmail("operator@datapro.com")) {
            appUserRepository.save(new AppUser(
                    "Operator User",
                    "operator@datapro.com",
                    passwordEncoder.encode("Operator123!"),
                    Role.ROLE_OPERATOR,
                    true
            ));
        }
    }
}

