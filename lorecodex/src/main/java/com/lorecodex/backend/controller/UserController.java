package com.lorecodex.backend.controller;

import com.lorecodex.backend.dto.request.LoginRequest;
import com.lorecodex.backend.dto.request.RegisterRequest;
import com.lorecodex.backend.dto.request.UpdateUserRequest;
import com.lorecodex.backend.dto.response.JwtAuthenticationResponse;
import com.lorecodex.backend.dto.response.UserResponse;
import com.lorecodex.backend.model.User;
import com.lorecodex.backend.service.AuthenticationService;
import com.lorecodex.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.login(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToUserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UpdateUserRequest request) {
        try {
            User updatedUser = userService.updateUserDetails(id, request);
            return ResponseEntity.ok(convertToUserResponse(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()))
                .build();
    }
}