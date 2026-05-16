package com.ticketmaster.userservice.service;

import com.ticketmaster.userservice.dto.request.UpdateUserRequest;
import com.ticketmaster.userservice.dto.response.UserResponse;
import com.ticketmaster.userservice.entity.User;
import com.ticketmaster.userservice.exception.ResourceNotFoundException;
import com.ticketmaster.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

public UserResponse getProfileByToken(String token) {
    String email = authService.extractEmailFromToken(token);
    return getProfile(email);
}

public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return authService.mapToUserResponse(user);
    }

    public Page<UserResponse> listUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable)
                .map(authService::mapToUserResponse);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user = userRepository.save(user);
        log.info("Admin updated user {} - role={} status={}", userId, user.getRole(), user.getStatus());
        return authService.mapToUserResponse(user);
    }
}
