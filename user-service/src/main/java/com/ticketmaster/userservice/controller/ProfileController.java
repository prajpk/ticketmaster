package com.ticketmaster.userservice.controller;

import com.ticketmaster.userservice.dto.response.UserResponse;
import com.ticketmaster.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        // Extract email from token manually - avoids Principal null issue
        String token = authHeader.substring(7);
        return ResponseEntity.ok(userService.getProfileByToken(token));
    }
}