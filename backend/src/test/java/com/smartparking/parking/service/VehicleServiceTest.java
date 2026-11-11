package com.smartparking.parking.service;

import com.smartparking.parking.dto.CreateVehicleRequest;
import com.smartparking.parking.dto.UpdateVehicleRequest;
import com.smartparking.parking.dto.VehicleBrandResponse;
import com.smartparking.parking.dto.VehicleModelResponse;
import com.smartparking.parking.dto.VehicleResponse;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.entity.Vehicle;
import com.smartparking.parking.entity.VehicleBrand;
import com.smartparking.parking.entity.VehicleCategory;
import com.smartparking.parking.entity.VehicleModel;
import com.smartparking.parking.exception.AccessDeniedException;
import com.smartparking.parking.exception.DuplicateResourceException;
import com.smartparking.parking.exception.InvalidVehicleException;
import com.smartparking.parking.exception.ResourceNotFoundException;
import com.smartparking.parking.repository.UserRepository;
import com.smartparking.parking.repository.VehicleBrandRepository;
import com.smartparking.parking.repository.VehicleModelRepository;
import com.smartparking.parking.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleBrandRepository brandRepository;

    @Mock
    private VehicleModelRepository modelRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private User normalUser;
    private User secondUser;
    private User adminUser;
    private VehicleBrand brandTata;
    private VehicleModel modelNexon;
    private VehicleModel modelSafari;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        normalUser = new User("User 1", "u1@example.com", "pass", "USER");
        normalUser.setId(1L);

        secondUser = new User("User 2", "u2@example.com", "pass", "USER");
        secondUser.setId(2L);

        adminUser = new User("Admin", "admin@example.com", "pass", "ADMIN");
        adminUser.setId(3L);

        brandTata = new VehicleBrand("Tata");
        brandTata.setId(10L);

        modelNexon = new VehicleModel("Nexon", brandTata, VehicleCategory.SUV);
        modelNexon.setId(100L);

        modelSafari = new VehicleModel("Safari", brandTata, VehicleCategory.LARGE_SUV);
        modelSafari.setId(200L);

        sampleVehicle = new Vehicle(normalUser, modelNexon, "KA01AB1234");
        sampleVehicle.setId(1000L);
        sampleVehicle.setActive(true);
    }

    // 1. Catalog Lookups

    @Test
    void getVehicleBrandsReturnsSortedBrands() {
        when(brandRepository.findAllByOrderByNameAsc()).thenReturn(List.of(brandTata));

        List<VehicleBrandResponse> brands = vehicleService.getVehicleBrands();

        assertEquals(1, brands.size());
        assertEquals("Tata", brands.get(0).getName());
    }

    @Test
    void getModelsByBrandReturnsModelsAndCategories() {
        when(brandRepository.existsById(10L)).thenReturn(true);
        when(modelRepository.findByBrandIdOrderByNameAsc(10L)).thenReturn(List.of(modelNexon, modelSafari));

        List<VehicleModelResponse> models = vehicleService.getModelsByBrand(10L);

        assertEquals(2, models.size());
        assertEquals("Nexon", models.get(0).getName());
        assertEquals(VehicleCategory.SUV, models.get(0).getCategory());
        assertEquals("Safari", models.get(1).getName());
        assertEquals(VehicleCategory.LARGE_SUV, models.get(1).getCategory());
    }

    @Test
    void getModelsByBrandNotFoundThrows404() {
        when(brandRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                vehicleService.getModelsByBrand(999L));
    }

    // 2. Create Vehicle

    @Test
    void createVehicleSuccessWithNormalizedRegistrationAndCategoryFromModel() {
        CreateVehicleRequest request = new CreateVehicleRequest(100L, "ka-05-xy-9999 ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(modelRepository.findById(100L)).thenReturn(Optional.of(modelNexon));
        when(vehicleRepository.existsByUserIdAndRegistrationNumberIgnoreCase(1L, "KA-05-XY-9999")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> {
            Vehicle v = i.getArgument(0);
            v.setId(500L);
            return v;
        });

        VehicleResponse response = vehicleService.createVehicle(request, 1L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("KA-05-XY-9999", response.getRegistrationNumber());
        assertEquals("Nexon", response.getModelName());
        assertEquals("Tata", response.getBrandName());
        assertEquals(VehicleCategory.SUV, response.getCategory());
        assertTrue(response.getActive());
    }

    @Test
    void createVehicleRejectsDuplicateRegistrationForSameUser() {
        CreateVehicleRequest request = new CreateVehicleRequest(100L, "KA01AB1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(modelRepository.findById(100L)).thenReturn(Optional.of(modelNexon));
        when(vehicleRepository.existsByUserIdAndRegistrationNumberIgnoreCase(1L, "KA01AB1234")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                vehicleService.createVehicle(request, 1L));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void createVehicleAdminRejected() {
        CreateVehicleRequest request = new CreateVehicleRequest(100L, "KA01AB1234");
        when(userRepository.findById(3L)).thenReturn(Optional.of(adminUser));

        assertThrows(AccessDeniedException.class, () ->
                vehicleService.createVehicle(request, 3L));
    }

    // 3. Get My Vehicles

    @Test
    void getMyVehiclesReturnsOnlyActiveVehicles() {
        Vehicle v2 = new Vehicle(normalUser, modelSafari, "KA02CD5678");
        v2.setId(1001L);
        v2.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(vehicleRepository.findByUserIdAndActiveTrue(1L)).thenReturn(List.of(sampleVehicle, v2));

        List<VehicleResponse> list = vehicleService.getMyVehicles(1L);

        assertEquals(2, list.size());
        assertEquals("KA01AB1234", list.get(0).getRegistrationNumber());
        assertEquals("KA02CD5678", list.get(1).getRegistrationNumber());
    }

    // 4. Get Vehicle By ID

    @Test
    void getVehicleByIdSuccessForOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));

        VehicleResponse response = vehicleService.getVehicleById(1000L, 1L);

        assertNotNull(response);
        assertEquals(1000L, response.getId());
        assertEquals("KA01AB1234", response.getRegistrationNumber());
    }

    @Test
    void getVehicleByIdForbiddenForAnotherUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(secondUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));

        assertThrows(AccessDeniedException.class, () ->
                vehicleService.getVehicleById(1000L, 2L));
    }

    @Test
    void getVehicleByIdInactiveTreatedAsNotFound() {
        sampleVehicle.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));

        assertThrows(ResourceNotFoundException.class, () ->
                vehicleService.getVehicleById(1000L, 1L));
    }

    // 5. Update Vehicle

    @Test
    void updateVehicleSuccess() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(200L, "KA01AB9999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));
        when(modelRepository.findById(200L)).thenReturn(Optional.of(modelSafari));
        when(vehicleRepository.existsByUserIdAndRegistrationNumberIgnoreCaseAndIdNot(1L, "KA01AB9999", 1000L)).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        VehicleResponse response = vehicleService.updateVehicle(1000L, request, 1L);

        assertEquals("KA01AB9999", response.getRegistrationNumber());
        assertEquals("Safari", response.getModelName());
        assertEquals(VehicleCategory.LARGE_SUV, response.getCategory()); // automatically changed with model
    }

    @Test
    void updateVehicleForbiddenForNonOwner() {
        UpdateVehicleRequest request = new UpdateVehicleRequest(200L, "KA01AB9999");

        when(userRepository.findById(2L)).thenReturn(Optional.of(secondUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));

        assertThrows(AccessDeniedException.class, () ->
                vehicleService.updateVehicle(1000L, request, 2L));
    }

    @Test
    void updateVehicleInactiveThrowsInvalidVehicleException() {
        sampleVehicle.setActive(false);
        UpdateVehicleRequest request = new UpdateVehicleRequest(200L, "KA01AB9999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));

        assertThrows(InvalidVehicleException.class, () ->
                vehicleService.updateVehicle(1000L, request, 1L));
    }

    // 6. Delete Vehicle (Soft Delete)

    @Test
    void deleteVehicleSetsActiveFalseWithoutDeletingRow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        VehicleResponse response = vehicleService.deleteVehicle(1000L, 1L);

        assertFalse(response.getActive());
        assertFalse(sampleVehicle.isActive());
        verify(vehicleRepository).save(sampleVehicle);
        verify(vehicleRepository, never()).delete(any());
        verify(vehicleRepository, never()).deleteById(any());
    }

    @Test
    void deleteAlreadyDeletedVehicleThrowsException() {
        sampleVehicle.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(vehicleRepository.findById(1000L)).thenReturn(Optional.of(sampleVehicle));

        assertThrows(InvalidVehicleException.class, () ->
                vehicleService.deleteVehicle(1000L, 1L));
    }
}
