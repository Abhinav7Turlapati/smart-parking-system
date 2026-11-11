package com.smartparking.parking.controller;

import com.smartparking.parking.dto.CreateParkingSpaceRequest;
import com.smartparking.parking.dto.PauseParkingSpaceRequest;
import com.smartparking.parking.dto.UpdateParkingSpaceRequest;
import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.entity.ParkingSpaceStatus;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.entity.VehicleCategory;
import com.smartparking.parking.repository.ParkingSpaceRepository;
import com.smartparking.parking.repository.UserRepository;
import com.smartparking.parking.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParkingSpaceControllerTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParkingSpaceRepository parkingSpaceRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private User secondUser;
    private User adminUser;
    private String userToken;
    private String secondUserToken;
    private String adminToken;

    private RestClient client() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(status -> true, (request, response) -> {})
                .build();
    }

    @BeforeEach
    void setUp() {
        String u1Email = "user1-" + UUID.randomUUID() + "@example.com";
        testUser = userRepository.save(new User("User One", u1Email, "pass123", "USER"));
        userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");

        String u2Email = "user2-" + UUID.randomUUID() + "@example.com";
        secondUser = userRepository.save(new User("User Two", u2Email, "pass123", "USER"));
        secondUserToken = jwtService.generateToken(secondUser.getId(), secondUser.getEmail(), "USER");

        String adminEmail = "admin-" + UUID.randomUUID() + "@example.com";
        adminUser = userRepository.save(new User("Admin User", adminEmail, "pass123", "ADMIN"));
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");
    }

    @Test
    void unauthenticatedAccessReturns401() {
        ResponseEntity<Map<String, Object>> response = client().get()
                .uri("/api/parking-spaces/my")
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void createParkingSpaceSuccessAndValidation() {
        CreateParkingSpaceRequest request = new CreateParkingSpaceRequest(
                "Spacious garage space",
                new BigDecimal("50.00"),
                "100 Brigade Road, Bangalore",
                12.9716,
                77.5946,
                VehicleCategory.SUV,
                "https://example.com/p1.jpg",
                "https://example.com/p2.jpg"
        );

        // 1. Success as USER
        ResponseEntity<Map<String, Object>> response = client().post()
                .uri("/api/parking-spaces")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(201, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get("id"));
        assertEquals("ACTIVE", body.get("status"));
        assertEquals("SUV", body.get("maximumVehicleCategory"));
        assertEquals(testUser.getId(), Long.valueOf(body.get("listedBy").toString()));

        // 2. ADMIN cannot create
        ResponseEntity<Map<String, Object>> adminResponse = client().post()
                .uri("/api/parking-spaces")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(403, adminResponse.getStatusCode().value());

        // 3. Validation failure
        CreateParkingSpaceRequest invalid = new CreateParkingSpaceRequest();
        invalid.setHourlyPrice(new BigDecimal("-5.00"));
        ResponseEntity<Map<String, Object>> badResponse = client().post()
                .uri("/api/parking-spaces")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalid)
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(400, badResponse.getStatusCode().value());
    }

    @Test
    void getMyParkingSpacesReturnsOnlyOwnSpaces() {
        ParkingSpace s1 = new ParkingSpace(
                testUser, "User 1 Space", new BigDecimal("40.00"), "Address 1",
                12.97, 77.59, VehicleCategory.SEDAN, "p1.jpg", null, ParkingSpaceStatus.ACTIVE
        );
        parkingSpaceRepository.save(s1);

        ParkingSpace s2 = new ParkingSpace(
                secondUser, "User 2 Space", new BigDecimal("60.00"), "Address 2",
                12.98, 77.60, VehicleCategory.SUV, "p2.jpg", null, ParkingSpaceStatus.ACTIVE
        );
        parkingSpaceRepository.save(s2);

        ResponseEntity<List<Map<String, Object>>> response = client().get()
                .uri("/api/parking-spaces/my")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(LIST_MAP_TYPE);

        assertEquals(200, response.getStatusCode().value());
        List<Map<String, Object>> list = response.getBody();
        assertNotNull(list);
        assertTrue(list.stream().allMatch(item ->
                Long.valueOf(item.get("listedBy").toString()).equals(testUser.getId())));
    }

    @Test
    void getParkingSpaceByIdReturns200Or404() {
        ParkingSpace s1 = new ParkingSpace(
                testUser, "Active Spot", new BigDecimal("40.00"), "Address 1",
                12.97, 77.59, VehicleCategory.SEDAN, "p1.jpg", null, ParkingSpaceStatus.ACTIVE
        );
        s1 = parkingSpaceRepository.save(s1);

        ParkingSpace removedSpace = new ParkingSpace(
                testUser, "Removed Spot", new BigDecimal("40.00"), "Address 2",
                12.97, 77.59, VehicleCategory.SEDAN, "p2.jpg", null, ParkingSpaceStatus.REMOVED
        );
        removedSpace = parkingSpaceRepository.save(removedSpace);

        // 1. Existing active space -> 200
        ResponseEntity<Map<String, Object>> response = client().get()
                .uri("/api/parking-spaces/" + s1.getId())
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, response.getStatusCode().value());

        // 2. Non-existent space -> 404
        ResponseEntity<Map<String, Object>> notFound = client().get()
                .uri("/api/parking-spaces/999999")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(404, notFound.getStatusCode().value());

        // 3. REMOVED space -> 404
        ResponseEntity<Map<String, Object>> removedResponse = client().get()
                .uri("/api/parking-spaces/" + removedSpace.getId())
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(404, removedResponse.getStatusCode().value());
    }

    @Test
    void updateParkingSpaceOwnershipAndValidation() {
        ParkingSpace s1 = new ParkingSpace(
                testUser, "Original Spot", new BigDecimal("40.00"), "Address 1",
                12.97, 77.59, VehicleCategory.SEDAN, "p1.jpg", null, ParkingSpaceStatus.ACTIVE
        );
        s1 = parkingSpaceRepository.save(s1);

        UpdateParkingSpaceRequest updateReq = new UpdateParkingSpaceRequest(
                "Updated Spot",
                new BigDecimal("75.00"),
                "New Address",
                12.95,
                77.55,
                VehicleCategory.LARGE_SUV,
                "p1_new.jpg",
                null
        );

        // 1. Owner updates -> 200 OK
        ResponseEntity<Map<String, Object>> okResp = client().put()
                .uri("/api/parking-spaces/" + s1.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateReq)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, okResp.getStatusCode().value());
        assertEquals("Updated Spot", okResp.getBody().get("description"));

        // 2. Non-owner updates -> 403 Forbidden
        ResponseEntity<Map<String, Object>> forbiddenResp = client().put()
                .uri("/api/parking-spaces/" + s1.getId())
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateReq)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(403, forbiddenResp.getStatusCode().value());

        // 3. Non-existent space -> 404
        ResponseEntity<Map<String, Object>> notFoundResp = client().put()
                .uri("/api/parking-spaces/999999")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateReq)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(404, notFoundResp.getStatusCode().value());
    }

    @Test
    void removeParkingSpaceSoftRemovesAndPreventsModification() {
        ParkingSpace space = new ParkingSpace(
                testUser, "To Remove", new BigDecimal("40.00"), "Address",
                12.97, 77.59, VehicleCategory.SEDAN, "p1.jpg", null, ParkingSpaceStatus.ACTIVE
        );
        space = parkingSpaceRepository.save(space);

        // 1. Non-owner cannot remove -> 403
        ResponseEntity<Map<String, Object>> forbiddenResp = client().delete()
                .uri("/api/parking-spaces/" + space.getId())
                .header("Authorization", "Bearer " + secondUserToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(403, forbiddenResp.getStatusCode().value());

        // 2. Owner removes -> 200 OK
        ResponseEntity<Map<String, Object>> okResp = client().delete()
                .uri("/api/parking-spaces/" + space.getId())
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, okResp.getStatusCode().value());
        assertEquals("REMOVED", okResp.getBody().get("status"));

        // Verify row still exists in DB
        ParkingSpace fromDb = parkingSpaceRepository.findById(space.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(ParkingSpaceStatus.REMOVED, fromDb.getStatus());

        // 3. Trying to remove again -> 400
        ResponseEntity<Map<String, Object>> badResp = client().delete()
                .uri("/api/parking-spaces/" + space.getId())
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(400, badResp.getStatusCode().value());
    }

    @Test
    void pauseAndResumeLifecycle() {
        ParkingSpace space = new ParkingSpace(
                testUser, "Pauseable Space", new BigDecimal("50.00"), "Address",
                12.97, 77.59, VehicleCategory.SEDAN, "p1.jpg", null, ParkingSpaceStatus.ACTIVE
        );
        space = parkingSpaceRepository.save(space);

        // 1. Pause 8 hours
        ResponseEntity<Map<String, Object>> pause8Resp = client().post()
                .uri("/api/parking-spaces/" + space.getId() + "/pause")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(PauseParkingSpaceRequest.eightHours())
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, pause8Resp.getStatusCode().value());
        assertEquals("PAUSED", pause8Resp.getBody().get("status"));
        assertNotNull(pause8Resp.getBody().get("pauseUntil"));

        // 2. Resume
        ResponseEntity<Map<String, Object>> resumeResp = client().post()
                .uri("/api/parking-spaces/" + space.getId() + "/resume")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, resumeResp.getStatusCode().value());
        assertEquals("ACTIVE", resumeResp.getBody().get("status"));
        assertNull(resumeResp.getBody().get("pauseUntil"));

        // 3. Pause indefinite
        ResponseEntity<Map<String, Object>> pauseIndefiniteResp = client().post()
                .uri("/api/parking-spaces/" + space.getId() + "/pause")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(PauseParkingSpaceRequest.indefinite())
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, pauseIndefiniteResp.getStatusCode().value());
        assertEquals("PAUSED", pauseIndefiniteResp.getBody().get("status"));
        assertNull(pauseIndefiniteResp.getBody().get("pauseUntil"));

        // 4. Invalid pause (past date) -> 400
        ResponseEntity<Map<String, Object>> invalidPauseResp = client().post()
                .uri("/api/parking-spaces/" + space.getId() + "/pause")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(PauseParkingSpaceRequest.custom(LocalDateTime.now().minusHours(1)))
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(400, invalidPauseResp.getStatusCode().value());
    }
}
