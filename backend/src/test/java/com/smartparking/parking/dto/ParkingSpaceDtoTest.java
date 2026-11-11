package com.smartparking.parking.dto;

import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.entity.ParkingSpaceStatus;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.entity.VehicleCategory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSpaceDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void compatibleVehicleCategoriesHierarchy() {
        // HATCHBACK
        List<VehicleCategory> hatchbackCompat = ParkingSpaceResponse.calculateCompatibleCategories(VehicleCategory.HATCHBACK);
        assertEquals(List.of(VehicleCategory.HATCHBACK), hatchbackCompat);

        // SEDAN
        List<VehicleCategory> sedanCompat = ParkingSpaceResponse.calculateCompatibleCategories(VehicleCategory.SEDAN);
        assertEquals(List.of(VehicleCategory.HATCHBACK, VehicleCategory.SEDAN), sedanCompat);

        // SUV
        List<VehicleCategory> suvCompat = ParkingSpaceResponse.calculateCompatibleCategories(VehicleCategory.SUV);
        assertEquals(List.of(VehicleCategory.HATCHBACK, VehicleCategory.SEDAN, VehicleCategory.SUV), suvCompat);

        // LARGE_SUV
        List<VehicleCategory> largeSuvCompat = ParkingSpaceResponse.calculateCompatibleCategories(VehicleCategory.LARGE_SUV);
        assertEquals(List.of(VehicleCategory.HATCHBACK, VehicleCategory.SEDAN, VehicleCategory.SUV, VehicleCategory.LARGE_SUV), largeSuvCompat);
    }

    @Test
    void createRequestValidationSucceedsForValidData() {
        CreateParkingSpaceRequest req = new CreateParkingSpaceRequest(
                "Covered private parking",
                new BigDecimal("50.00"),
                "123 MG Road, Bangalore",
                12.9716,
                77.5946,
                VehicleCategory.SEDAN,
                "https://example.com/p1.jpg",
                "https://example.com/p2.jpg"
        );

        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void createRequestValidationFailsForInvalidData() {
        CreateParkingSpaceRequest req = new CreateParkingSpaceRequest();
        req.setDescription("");
        req.setHourlyPrice(new BigDecimal("-10.00"));
        req.setAddress("   ");
        req.setLatitude(95.0); // beyond 90
        req.setLongitude(-190.0); // beyond -180
        req.setMaximumVehicleCategory(null);
        req.setPhoto1("");

        var violations = validator.validate(req);
        assertEquals(7, violations.size());
    }

    @Test
    void responseDtoCalculatesExpiredPauseAsActive() {
        User owner = new User("Owner", "owner@example.com", "pass", "USER");
        owner.setId(10L);

        ParkingSpace space = new ParkingSpace(
                owner,
                "Test description",
                new BigDecimal("40.00"),
                "Park Street",
                12.0,
                77.0,
                VehicleCategory.SUV,
                "photo1.jpg",
                null,
                ParkingSpaceStatus.PAUSED
        );
        space.setId(1L);
        space.setPauseUntil(LocalDateTime.now().minusHours(1)); // Expired pause

        ParkingSpaceResponse response = ParkingSpaceResponse.fromEntity(space);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getListedBy());
        assertEquals(10L, response.getListedById());
        assertEquals(ParkingSpaceStatus.ACTIVE, response.getStatus());
        assertNull(response.getPauseUntil());
        assertEquals(3, response.getCompatibleVehicleCategories().size());
    }
}
