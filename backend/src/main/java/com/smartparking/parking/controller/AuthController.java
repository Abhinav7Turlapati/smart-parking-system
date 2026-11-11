package com.smartparking.parking.controller;

import com.smartparking.parking.dto.AuthResponse;
import com.smartparking.parking.dto.LoginRequest;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.security.UserPrincipal;
import com.smartparking.parking.service.AuthService;
import com.smartparking.parking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) {

        User registeredUser = userService.registerUser(user);

        AuthResponse response = new AuthResponse(
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail(),
                registeredUser.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Temporary JWT verification endpoint (not business functionality).
     * Requires a valid Bearer token; used to confirm SecurityContext authentication.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(Map.of(
                "id", principal.getId(),
                "email", principal.getEmail(),
                "role", principal.getRole()
        ));
    }
}