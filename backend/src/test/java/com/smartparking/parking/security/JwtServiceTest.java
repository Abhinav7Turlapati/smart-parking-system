package com.smartparking.parking.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String TEST_SECRET = "unit-test-jwt-secret-key-at-least-256-bits-long-xxxx";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 3_600_000L);
    }

    @Test
    void generateTokenIncludesRequiredClaimsAndOmitsPassword() {
        String token = jwtService.generateToken(42L, "user@example.com", "USER");

        Claims claims = jwtService.extractAllClaims(token);

        assertEquals(42L, claims.get("userId", Number.class).longValue());
        assertEquals("user@example.com", claims.get("email", String.class));
        assertEquals("user@example.com", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
        assertNotNull(claims.getExpiration());
        assertNull(claims.get("password"));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void expiredTokenIsRejected() {
        String token = jwtService.generateToken(1L, "expired@example.com", "ADMIN", -1_000L);

        assertTrue(jwtService.isTokenExpired(token));
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(1L, "user@example.com", "USER");

        assertFalse(jwtService.isTokenValid(token + "tampered"));
    }
}
