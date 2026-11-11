package com.smartparking.parking.service;

import com.smartparking.parking.dto.CreateVehicleRequest;
import com.smartparking.parking.dto.UpdateVehicleRequest;
import com.smartparking.parking.dto.VehicleBrandResponse;
import com.smartparking.parking.dto.VehicleModelResponse;
import com.smartparking.parking.dto.VehicleResponse;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.entity.Vehicle;
import com.smartparking.parking.entity.VehicleModel;
import com.smartparking.parking.exception.AccessDeniedException;
import com.smartparking.parking.exception.DuplicateResourceException;
import com.smartparking.parking.exception.InvalidVehicleException;
import com.smartparking.parking.exception.ResourceNotFoundException;
import com.smartparking.parking.repository.UserRepository;
import com.smartparking.parking.repository.VehicleBrandRepository;
import com.smartparking.parking.repository.VehicleModelRepository;
import com.smartparking.parking.repository.VehicleRepository;
import com.smartparking.parking.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VehicleService {

    private final VehicleBrandRepository brandRepository;
    private final VehicleModelRepository modelRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleBrandRepository brandRepository,
                          VehicleModelRepository modelRepository,
                          VehicleRepository vehicleRepository,
                          UserRepository userRepository) {
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    /**
     * 1. Get all vehicle brands sorted by name.
     */
    @Transactional(readOnly = true)
    public List<VehicleBrandResponse> getVehicleBrands() {
        return brandRepository.findAllByOrderByNameAsc().stream()
                .map(VehicleBrandResponse::fromEntity)
                .toList();
    }

    /**
     * 2. Get all models belonging to a specific brand.
     */
    @Transactional(readOnly = true)
    public List<VehicleModelResponse> getModelsByBrand(Long brandId) {
        if (brandId == null) {
            throw new ResourceNotFoundException("Vehicle brand ID cannot be null");
        }
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Vehicle brand not found with id: " + brandId);
        }
        return modelRepository.findByBrandIdOrderByNameAsc(brandId).stream()
                .map(VehicleModelResponse::fromEntity)
                .toList();
    }

    /**
     * 3. Create a vehicle.
     * - Authenticated USER only; ADMIN cannot create user vehicles.
     * - Category is derived from VehicleModel; client must not provide category.
     * - Registration number normalized uppercase and checked for duplicates for this user.
     */
    public VehicleResponse createVehicle(CreateVehicleRequest request, Long currentUserId) {
        if (request == null) {
            throw new InvalidVehicleException("Create vehicle request body cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);

        VehicleModel model = modelRepository.findById(request.getVehicleModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle model not found with id: " + request.getVehicleModelId()));

        String normalizedReg = normalizeRegistrationNumber(request.getRegistrationNumber());

        if (vehicleRepository.existsByUserIdAndRegistrationNumberIgnoreCase(user.getId(), normalizedReg)) {
            throw new DuplicateResourceException("Registration number '" + normalizedReg + "' already exists for this user");
        }

        Vehicle vehicle = new Vehicle(user, model, normalizedReg);
        vehicle.setActive(true);

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(saved);
    }

    public VehicleResponse createVehicle(CreateVehicleRequest request, UserPrincipal principal) {
        validatePrincipal(principal);
        return createVehicle(request, principal.getId());
    }

    /**
     * 4. Get active vehicles belonging to the authenticated USER.
     * Soft-deleted vehicles are excluded.
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles(Long currentUserId) {
        validateAndGetNormalUser(currentUserId);

        return vehicleRepository.findByUserIdAndActiveTrue(currentUserId).stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    public List<VehicleResponse> getMyVehicles(UserPrincipal principal) {
        validatePrincipal(principal);
        return getMyVehicles(principal.getId());
    }

    /**
     * 5. Get vehicle by ID.
     * User can view only their own vehicle.
     * Inactive/soft-deleted vehicles are treated as unavailable (404).
     */
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long vehicleId, Long currentUserId) {
        if (vehicleId == null) {
            throw new ResourceNotFoundException("Vehicle ID cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        verifyOwnership(vehicle, user);

        if (!vehicle.isActive()) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + vehicleId);
        }

        return VehicleResponse.fromEntity(vehicle);
    }

    public VehicleResponse getVehicleById(Long vehicleId, UserPrincipal principal) {
        validatePrincipal(principal);
        return getVehicleById(vehicleId, principal.getId());
    }

    /**
     * 6. Update vehicle.
     * User can update only their own active vehicle.
     * Model change automatically updates category.
     * Re-checks duplicate registration number for this user.
     */
    public VehicleResponse updateVehicle(Long vehicleId, UpdateVehicleRequest request, Long currentUserId) {
        if (vehicleId == null) {
            throw new ResourceNotFoundException("Vehicle ID cannot be null");
        }
        if (request == null) {
            throw new InvalidVehicleException("Update vehicle request body cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        verifyOwnership(vehicle, user);

        if (!vehicle.isActive()) {
            throw new InvalidVehicleException("Cannot update an inactive or deleted vehicle");
        }

        VehicleModel model = modelRepository.findById(request.getVehicleModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle model not found with id: " + request.getVehicleModelId()));

        String normalizedReg = normalizeRegistrationNumber(request.getRegistrationNumber());

        if (vehicleRepository.existsByUserIdAndRegistrationNumberIgnoreCaseAndIdNot(user.getId(), normalizedReg, vehicleId)) {
            throw new DuplicateResourceException("Registration number '" + normalizedReg + "' is already registered to another vehicle for this user");
        }

        vehicle.setVehicleModel(model);
        vehicle.setRegistrationNumber(normalizedReg);

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(saved);
    }

    public VehicleResponse updateVehicle(Long vehicleId, UpdateVehicleRequest request, UserPrincipal principal) {
        validatePrincipal(principal);
        return updateVehicle(vehicleId, request, principal.getId());
    }

    /**
     * 7. Soft delete vehicle.
     * User can delete only their own vehicle.
     * Sets active = false without physically deleting the database row.
     */
    public VehicleResponse deleteVehicle(Long vehicleId, Long currentUserId) {
        if (vehicleId == null) {
            throw new ResourceNotFoundException("Vehicle ID cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        verifyOwnership(vehicle, user);

        if (!vehicle.isActive()) {
            throw new InvalidVehicleException("Vehicle is already deleted");
        }

        vehicle.setActive(false);

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(saved);
    }

    public VehicleResponse deleteVehicle(Long vehicleId, UserPrincipal principal) {
        validatePrincipal(principal);
        return deleteVehicle(vehicleId, principal.getId());
    }

    // Helper validation methods

    private User validateAndGetNormalUser(Long userId) {
        if (userId == null) {
            throw new ResourceNotFoundException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException("Admins are not allowed to manage user vehicles");
        }

        return user;
    }

    private void validatePrincipal(UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("User must be authenticated");
        }
    }

    private void verifyOwnership(Vehicle vehicle, User user) {
        if (vehicle.getUser() == null || !vehicle.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access or modify this vehicle");
        }
    }

    private String normalizeRegistrationNumber(String reg) {
        if (reg == null || reg.trim().isEmpty()) {
            throw new InvalidVehicleException("Registration number cannot be blank");
        }
        return reg.trim().toUpperCase();
    }
}
