package com.datapro.dataimporter.security.service;

import com.datapro.dataimporter.security.dto.UserSummaryResponse;
import com.datapro.dataimporter.security.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<UserSummaryResponse> findAll() {
        return appUserRepository.findAll()
                .stream()
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .toList();
    }
}

