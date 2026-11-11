package com.smartparking.parking.controller;

import com.smartparking.parking.dto.CreateVehicleRequest;
import com.smartparking.parking.dto.UpdateVehicleRequest;
import com.smartparking.parking.dto.VehicleBrandResponse;
import com.smartparking.parking.dto.VehicleModelResponse;
import com.smartparking.parking.dto.VehicleResponse;
import com.smartparking.parking.exception.AccessDeniedException;
import com.smartparking.parking.security.UserPrincipal;
import com.smartparking.parking.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * 1. Get all vehicle brands.
     * GET /api/vehicles/brands
     */
    @GetMapping("/brands")
    public ResponseEntity<List<VehicleBrandResponse>> getVehicleBrands(
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        List<VehicleBrandResponse> brands = vehicleService.getVehicleBrands();
        return ResponseEntity.ok(brands);
    }

    /**
     * 2. Get all models by brand ID.
     * GET /api/vehicles/brands/{brandId}/models
     */
    @GetMapping("/brands/{brandId}/models")
    public ResponseEntity<List<VehicleModelResponse>> getModelsByBrand(
            @PathVariable("brandId") Long brandId,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        List<VehicleModelResponse> models = vehicleService.getModelsByBrand(brandId);
        return ResponseEntity.ok(models);
    }

    /**
     * 3. Get my active vehicles.
     * GET /api/vehicles/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<VehicleResponse>> getMyVehicles(
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        List<VehicleResponse> vehicles = vehicleService.getMyVehicles(principal.getId());
        return ResponseEntity.ok(vehicles);
    }

    /**
     * 4. Get a specific vehicle by ID.
     * GET /api/vehicles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        VehicleResponse response = vehicleService.getVehicleById(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 5. Create a new vehicle.
     * POST /api/vehicles
     */
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        VehicleResponse response = vehicleService.createVehicle(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 6. Update a vehicle.
     * PUT /api/vehicles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateVehicleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        VehicleResponse response = vehicleService.updateVehicle(id, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 7. Soft delete a vehicle.
     * DELETE /api/vehicles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<VehicleResponse> deleteVehicle(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        VehicleResponse response = vehicleService.deleteVehicle(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    private void validateAuthenticated(UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required");
        }
    }
}
