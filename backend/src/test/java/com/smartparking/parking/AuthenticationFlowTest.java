package com.smartparking.parking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationFlowTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @LocalServerPort
    private int port;

    private RestClient client() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(status -> true, (request, response) -> {
                })
                .build();
    }

    @Test
    void healthIsPublic() {
        ResponseEntity<Map<String, Object>> response = client().get()
                .uri("/api/health")
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    void registerIgnoresRequestedAdminRoleAndLoginIssuesJwt() {
        String email = "authflow-" + UUID.randomUUID() + "@example.com";
        String password = "TestPass123!";
        RestClient client = client();

        ResponseEntity<Map<String, Object>> registerResponse = client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "name", "Flow User",
                        "email", email,
                        "password", password,
                        "role", "ADMIN"
                ))
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(200, registerResponse.getStatusCode().value());
        Map<String, Object> registered = registerResponse.getBody();
        assertNotNull(registered);
        assertEquals(email, registered.get("email"));
        assertEquals("USER", registered.get("role"));
        assertNull(registered.get("token"));
        assertFalse(registered.containsKey("password"));

        ResponseEntity<Map<String, Object>> loginResponse = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "email", email,
                        "password", password
                ))
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(200, loginResponse.getStatusCode().value());
        Map<String, Object> loginBody = loginResponse.getBody();
        assertNotNull(loginBody);
        String token = (String) loginBody.get("token");
        assertNotNull(token);
        assertEquals("Flow User", loginBody.get("name"));
        assertEquals(email, loginBody.get("email"));
        assertEquals("USER", loginBody.get("role"));
        assertFalse(loginBody.containsKey("password"));

        ResponseEntity<Map<String, Object>> unauthenticated = client.get()
                .uri("/api/auth/me")
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(401, unauthenticated.getStatusCode().value());

        ResponseEntity<Map<String, Object>> me = client.get()
                .uri("/api/auth/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(200, me.getStatusCode().value());
        Map<String, Object> meBody = me.getBody();
        assertNotNull(meBody);
        assertEquals(email, meBody.get("email"));
        assertEquals("USER", meBody.get("role"));
        assertFalse(meBody.containsKey("password"));
    }

    @Test
    void loginRejectsInvalidCredentialsWithoutRevealingEmail() {
        ResponseEntity<Map<String, Object>> response = client().post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "email", "missing-user@example.com",
                        "password", "wrong-password"
                ))
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid email or password", response.getBody().get("error"));
    }

    @Test
    void loginRejectsInvalidEmailFormat() {
        ResponseEntity<Map<String, Object>> response = client().post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "email", "not-an-email",
                        "password", "whatever"
                ))
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(400, response.getStatusCode().value());
    }
}
