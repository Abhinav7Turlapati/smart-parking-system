package com.smartparking.parking.service;

import com.smartparking.parking.dto.AuthResponse;
import com.smartparking.parking.dto.LoginRequest;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.repository.UserRepository;
import com.smartparking.parking.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        // Generic error message to never reveal whether the email exists
        final String invalidCredentialsMessage = "Invalid email or password";

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new BadCredentialsException(invalidCredentialsMessage);
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(invalidCredentialsMessage);
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
