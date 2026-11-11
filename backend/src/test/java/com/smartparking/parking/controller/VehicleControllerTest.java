package com.smartparking.parking.controller;

import com.smartparking.parking.dto.CreateVehicleRequest;
import com.smartparking.parking.dto.UpdateVehicleRequest;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.entity.Vehicle;
import com.smartparking.parking.entity.VehicleBrand;
import com.smartparking.parking.entity.VehicleCategory;
import com.smartparking.parking.entity.VehicleModel;
import com.smartparking.parking.repository.UserRepository;
import com.smartparking.parking.repository.VehicleBrandRepository;
import com.smartparking.parking.repository.VehicleModelRepository;
import com.smartparking.parking.repository.VehicleRepository;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VehicleControllerTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleBrandRepository brandRepository;

    @Autowired
    private VehicleModelRepository modelRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private User secondUser;
    private User adminUser;
    private String userToken;
    private String secondUserToken;
    private String adminToken;

    private VehicleBrand sampleBrand;
    private VehicleModel sampleModel;
    private VehicleModel secondModel;

    private RestClient client() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(status -> true, (request, response) -> {})
                .build();
    }

    @BeforeEach
    void setUp() {
        String u1Email = "v-user1-" + UUID.randomUUID() + "@example.com";
        testUser = userRepository.save(new User("Vehicle User One", u1Email, "pass123", "USER"));
        userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");

        String u2Email = "v-user2-" + UUID.randomUUID() + "@example.com";
        secondUser = userRepository.save(new User("Vehicle User Two", u2Email, "pass123", "USER"));
        secondUserToken = jwtService.generateToken(secondUser.getId(), secondUser.getEmail(), "USER");

        String adminEmail = "v-admin-" + UUID.randomUUID() + "@example.com";
        adminUser = userRepository.save(new User("Vehicle Admin", adminEmail, "pass123", "ADMIN"));
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");

        sampleBrand = brandRepository.findByNameIgnoreCase("Tata").orElseGet(() ->
                brandRepository.save(new VehicleBrand("Tata")));

        sampleModel = modelRepository.findByBrandIdAndNameIgnoreCase(sampleBrand.getId(), "Nexon").orElseGet(() ->
                modelRepository.save(new VehicleModel("Nexon", sampleBrand, VehicleCategory.SUV)));

        secondModel = modelRepository.findByBrandIdAndNameIgnoreCase(sampleBrand.getId(), "Safari").orElseGet(() ->
                modelRepository.save(new VehicleModel("Safari", sampleBrand, VehicleCategory.LARGE_SUV)));
    }

    // 8. Unauthorized request
    @Test
    void unauthenticatedAccessReturns401() {
        ResponseEntity<Map<String, Object>> response = client().get()
                .uri("/api/vehicles/my")
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(401, response.getStatusCode().value());
    }

    // 1. GET brands
    @Test
    void getVehicleBrandsReturns200WithCatalog() {
        ResponseEntity<List<Map<String, Object>>> response = client().get()
                .uri("/api/vehicles/brands")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(LIST_MAP_TYPE);

        assertEquals(200, response.getStatusCode().value());
        List<Map<String, Object>> brands = response.getBody();
        assertNotNull(brands);
        assertTrue(brands.size() >= 10);
    }

    // 2. GET models by brand
    @Test
    void getModelsByBrandReturnsModelsAndCategories() {
        ResponseEntity<List<Map<String, Object>>> response = client().get()
                .uri("/api/vehicles/brands/" + sampleBrand.getId() + "/models")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(LIST_MAP_TYPE);

        assertEquals(200, response.getStatusCode().value());
        List<Map<String, Object>> models = response.getBody();
        assertNotNull(models);
        assertFalse(models.isEmpty());
        assertTrue(models.stream().anyMatch(m -> "Nexon".equals(m.get("name")) && "SUV".equals(m.get("category"))));
    }

    // 3. Create vehicle & 10. Duplicate registration number rejected
    @Test
    void createVehicleLifecycleAndDuplicateRejection() {
        CreateVehicleRequest request = new CreateVehicleRequest(sampleModel.getId(), "ka 01 ab 1234");

        // Success -> 201 Created
        ResponseEntity<Map<String, Object>> response = client().post()
                .uri("/api/vehicles")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(201, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get("id"));
        assertEquals("KA 01 AB 1234", body.get("registrationNumber"));
        assertEquals("Nexon", body.get("modelName"));
        assertEquals("SUV", body.get("category"));
        assertTrue((Boolean) body.get("active"));

        // Duplicate registration for same user -> 400 Bad Request
        ResponseEntity<Map<String, Object>> duplicateResp = client().post()
                .uri("/api/vehicles")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateVehicleRequest(sampleModel.getId(), "KA 01 AB 1234"))
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(400, duplicateResp.getStatusCode().value());

        // Same registration number by another user is allowed
        ResponseEntity<Map<String, Object>> secondUserResp = client().post()
                .uri("/api/vehicles")
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateVehicleRequest(sampleModel.getId(), "KA 01 AB 1234"))
                .retrieve()
                .toEntity(MAP_TYPE);

        assertEquals(201, secondUserResp.getStatusCode().value());
    }

    // 4. Get my vehicles
    @Test
    void getMyVehiclesReturnsOnlyUserVehicles() {
        Vehicle v1 = vehicleRepository.save(new Vehicle(testUser, sampleModel, "KA 05 M 1111"));
        Vehicle v2 = vehicleRepository.save(new Vehicle(secondUser, sampleModel, "KA 05 M 2222"));

        ResponseEntity<List<Map<String, Object>>> response = client().get()
                .uri("/api/vehicles/my")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(LIST_MAP_TYPE);

        assertEquals(200, response.getStatusCode().value());
        List<Map<String, Object>> list = response.getBody();
        assertNotNull(list);
        assertTrue(list.stream().allMatch(v -> testUser.getId().equals(Long.valueOf(v.get("userId").toString()))));
    }

    // 5. Get own vehicle & 9. Another user cannot access another user's vehicle
    @Test
    void getVehicleByIdOwnershipEnforcement() {
        Vehicle vehicle = vehicleRepository.save(new Vehicle(testUser, sampleModel, "KA 03 AA 5555"));

        // Owner accesses -> 200 OK
        ResponseEntity<Map<String, Object>> okResp = client().get()
                .uri("/api/vehicles/" + vehicle.getId())
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, okResp.getStatusCode().value());

        // Another user accesses -> 403 Forbidden
        ResponseEntity<Map<String, Object>> forbiddenResp = client().get()
                .uri("/api/vehicles/" + vehicle.getId())
                .header("Authorization", "Bearer " + secondUserToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(403, forbiddenResp.getStatusCode().value());

        // Non-existent vehicle -> 404 Not Found
        ResponseEntity<Map<String, Object>> notFoundResp = client().get()
                .uri("/api/vehicles/999999")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(404, notFoundResp.getStatusCode().value());
    }

    // 6. Update own vehicle
    @Test
    void updateVehicleSuccessAndOwnershipCheck() {
        Vehicle vehicle = vehicleRepository.save(new Vehicle(testUser, sampleModel, "KA 04 BB 6666"));

        UpdateVehicleRequest updateReq = new UpdateVehicleRequest(secondModel.getId(), "KA 04 BB 7777");

        // Non-owner update -> 403 Forbidden
        ResponseEntity<Map<String, Object>> forbiddenResp = client().put()
                .uri("/api/vehicles/" + vehicle.getId())
                .header("Authorization", "Bearer " + secondUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateReq)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(403, forbiddenResp.getStatusCode().value());

        // Owner update -> 200 OK, category updated to LARGE_SUV
        ResponseEntity<Map<String, Object>> okResp = client().put()
                .uri("/api/vehicles/" + vehicle.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateReq)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, okResp.getStatusCode().value());
        Map<String, Object> body = okResp.getBody();
        assertNotNull(body);
        assertEquals("KA 04 BB 7777", body.get("registrationNumber"));
        assertEquals("Safari", body.get("modelName"));
        assertEquals("LARGE_SUV", body.get("category"));
    }

    // 7. Delete vehicle & 11. Deleted vehicle does not appear in My Vehicles
    @Test
    void deleteVehicleSoftDeletesAndExcludesFromMyVehicles() {
        Vehicle vehicle = vehicleRepository.save(new Vehicle(testUser, sampleModel, "KA 01 ZZ 9999"));

        // Non-owner delete -> 403 Forbidden
        ResponseEntity<Map<String, Object>> forbiddenResp = client().delete()
                .uri("/api/vehicles/" + vehicle.getId())
                .header("Authorization", "Bearer " + secondUserToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(403, forbiddenResp.getStatusCode().value());

        // Owner delete -> 200 OK
        ResponseEntity<Map<String, Object>> okResp = client().delete()
                .uri("/api/vehicles/" + vehicle.getId())
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(200, okResp.getStatusCode().value());
        assertFalse((Boolean) okResp.getBody().get("active"));

        // Deleted vehicle does not appear in My Vehicles
        ResponseEntity<List<Map<String, Object>>> myVehiclesResp = client().get()
                .uri("/api/vehicles/my")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(LIST_MAP_TYPE);
        assertEquals(200, myVehiclesResp.getStatusCode().value());
        List<Map<String, Object>> myVehicles = myVehiclesResp.getBody();
        assertNotNull(myVehicles);
        assertTrue(myVehicles.stream().noneMatch(v -> vehicle.getId().equals(Long.valueOf(v.get("id").toString()))));

        // GET /api/vehicles/{id} on soft-deleted vehicle returns 404
        ResponseEntity<Map<String, Object>> getDeletedResp = client().get()
                .uri("/api/vehicles/" + vehicle.getId())
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .toEntity(MAP_TYPE);
        assertEquals(404, getDeletedResp.getStatusCode().value());
    }
}
