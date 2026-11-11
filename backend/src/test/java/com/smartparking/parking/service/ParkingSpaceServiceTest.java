package com.smartparking.parking.service;

import com.smartparking.parking.dto.CreateParkingSpaceRequest;
import com.smartparking.parking.dto.ParkingSpaceResponse;
import com.smartparking.parking.dto.PauseParkingSpaceRequest;
import com.smartparking.parking.dto.UpdateParkingSpaceRequest;
import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.entity.ParkingSpaceStatus;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.entity.VehicleCategory;
import com.smartparking.parking.exception.AccessDeniedException;
import com.smartparking.parking.exception.InvalidParkingSpaceException;
import com.smartparking.parking.exception.InvalidPauseRequestException;
import com.smartparking.parking.exception.ResourceNotFoundException;
import com.smartparking.parking.repository.ParkingSpaceRepository;
import com.smartparking.parking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpaceServiceTest {

    @Mock
    private ParkingSpaceRepository parkingSpaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ParkingSpaceService parkingSpaceService;

    private User normalUser;
    private User adminUser;
    private User anotherUser;
    private ParkingSpace sampleSpace;

    @BeforeEach
    void setUp() {
        normalUser = new User("John", "john@example.com", "pass", "USER");
        normalUser.setId(1L);

        adminUser = new User("Admin", "admin@example.com", "pass", "ADMIN");
        adminUser.setId(2L);

        anotherUser = new User("Alice", "alice@example.com", "pass", "USER");
        anotherUser.setId(3L);

        sampleSpace = new ParkingSpace(
                normalUser,
                "Central parking spot",
                new BigDecimal("60.00"),
                "MG Road, Block A",
                12.9716,
                77.5946,
                VehicleCategory.SEDAN,
                "photo1.jpg",
                "photo2.jpg",
                ParkingSpaceStatus.ACTIVE
        );
        sampleSpace.setId(100L);
    }

    // 1. Create Parking Space Tests

    @Test
    void createParkingSpaceSuccess() {
        CreateParkingSpaceRequest request = new CreateParkingSpaceRequest(
                "Central parking spot",
                new BigDecimal("60.00"),
                "MG Road, Block A",
                12.9716,
                77.5946,
                VehicleCategory.SEDAN,
                "photo1.jpg",
                "photo2.jpg"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenAnswer(invocation -> {
            ParkingSpace toSave = invocation.getArgument(0);
            toSave.setId(100L);
            return toSave;
        });

        ParkingSpaceResponse response = parkingSpaceService.createParkingSpace(request, 1L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getListedBy());
        assertEquals(ParkingSpaceStatus.ACTIVE, response.getStatus());
        assertEquals(VehicleCategory.SEDAN, response.getMaximumVehicleCategory());
        assertEquals(2, response.getCompatibleVehicleCategories().size());
        verify(parkingSpaceRepository).save(any(ParkingSpace.class));
    }

    @Test
    void createParkingSpaceAdminForbidden() {
        CreateParkingSpaceRequest request = new CreateParkingSpaceRequest(
                "Spot", new BigDecimal("10.00"), "Road", 10.0, 20.0, VehicleCategory.HATCHBACK, "p1", null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThrows(AccessDeniedException.class, () ->
                parkingSpaceService.createParkingSpace(request, 2L));
        verify(parkingSpaceRepository, never()).save(any());
    }

    // 2. Get My Parking Spaces Tests

    @Test
    void getMyParkingSpacesReturnsUserSpacesAndRefreshesExpiredPause() {
        ParkingSpace pausedExpired = new ParkingSpace(
                normalUser, "Expired pause space", new BigDecimal("30.00"), "Street",
                10.0, 20.0, VehicleCategory.HATCHBACK, "p1", null, ParkingSpaceStatus.PAUSED
        );
        pausedExpired.setId(101L);
        pausedExpired.setPauseUntil(LocalDateTime.now().minusHours(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findByOwnerId(1L)).thenReturn(List.of(sampleSpace, pausedExpired));
        when(parkingSpaceRepository.save(pausedExpired)).thenReturn(pausedExpired);

        List<ParkingSpaceResponse> result = parkingSpaceService.getMyParkingSpaces(1L);

        assertEquals(2, result.size());
        assertEquals(ParkingSpaceStatus.ACTIVE, result.get(0).getStatus());
        assertEquals(ParkingSpaceStatus.ACTIVE, result.get(1).getStatus()); // Expired pause refreshed to ACTIVE
    }

    @Test
    void getMyParkingSpacesRejectsAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThrows(AccessDeniedException.class, () ->
                parkingSpaceService.getMyParkingSpaces(2L));
    }

    // 3. Get Parking Space By ID Tests

    @Test
    void getParkingSpaceByIdSuccess() {
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));

        ParkingSpaceResponse response = parkingSpaceService.getParkingSpaceById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Central parking spot", response.getDescription());
    }

    @Test
    void getParkingSpaceByIdNotFoundThrows404() {
        when(parkingSpaceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                parkingSpaceService.getParkingSpaceById(999L));
    }

    @Test
    void getParkingSpaceByIdRemovedThrows404() {
        sampleSpace.setStatus(ParkingSpaceStatus.REMOVED);
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));

        assertThrows(ResourceNotFoundException.class, () ->
                parkingSpaceService.getParkingSpaceById(100L));
    }

    // 4. Update Parking Space Tests

    @Test
    void updateParkingSpaceSuccess() {
        UpdateParkingSpaceRequest updateRequest = new UpdateParkingSpaceRequest(
                "Updated Spot",
                new BigDecimal("80.00"),
                "New Address",
                13.0,
                78.0,
                VehicleCategory.SUV,
                "new1.jpg",
                "new2.jpg"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSpaceResponse response = parkingSpaceService.updateParkingSpace(100L, updateRequest, 1L);

        assertEquals("Updated Spot", response.getDescription());
        assertEquals(new BigDecimal("80.00"), response.getHourlyPrice());
        assertEquals("New Address", response.getAddress());
        assertEquals(VehicleCategory.SUV, response.getMaximumVehicleCategory());
        assertEquals(3, response.getCompatibleVehicleCategories().size());
        assertEquals(1L, response.getListedBy()); // owner unchanged
    }

    @Test
    void updateParkingSpaceByAnotherUserForbidden() {
        UpdateParkingSpaceRequest updateRequest = new UpdateParkingSpaceRequest(
                "Updated", new BigDecimal("50.00"), "Addr", 10.0, 20.0, VehicleCategory.SEDAN, "p1", null
        );

        when(userRepository.findById(3L)).thenReturn(Optional.of(anotherUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));

        assertThrows(AccessDeniedException.class, () ->
                parkingSpaceService.updateParkingSpace(100L, updateRequest, 3L));
    }

    @Test
    void updateParkingSpaceRemovedThrowsBadRequest() {
        sampleSpace.setStatus(ParkingSpaceStatus.REMOVED);
        UpdateParkingSpaceRequest updateRequest = new UpdateParkingSpaceRequest(
                "Updated", new BigDecimal("50.00"), "Addr", 10.0, 20.0, VehicleCategory.SEDAN, "p1", null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));

        assertThrows(InvalidParkingSpaceException.class, () ->
                parkingSpaceService.updateParkingSpace(100L, updateRequest, 1L));
    }

    // 5. Remove Parking Space Tests

    @Test
    void removeParkingSpacePerformsSoftRemoval() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSpaceResponse response = parkingSpaceService.removeParkingSpace(100L, 1L);

        assertEquals(ParkingSpaceStatus.REMOVED, response.getStatus());
        assertEquals(ParkingSpaceStatus.REMOVED, sampleSpace.getStatus());
        verify(parkingSpaceRepository).save(sampleSpace);
        verify(parkingSpaceRepository, never()).delete(any());
        verify(parkingSpaceRepository, never()).deleteById(any());
    }

    @Test
    void removeAlreadyRemovedSpaceThrowsBadRequest() {
        sampleSpace.setStatus(ParkingSpaceStatus.REMOVED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));

        assertThrows(InvalidParkingSpaceException.class, () ->
                parkingSpaceService.removeParkingSpace(100L, 1L));
    }

    // 6. Pause and Resume Tests

    @Test
    void pauseParkingSpace8HoursSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSpaceResponse response = parkingSpaceService.pauseParkingSpace(100L, 1L, PauseParkingSpaceRequest.eightHours());

        assertEquals(ParkingSpaceStatus.PAUSED, response.getStatus());
        assertNotNull(response.getPauseUntil());
        assertTrue(response.getPauseUntil().isAfter(LocalDateTime.now().plusHours(7)));
    }

    @Test
    void pauseParkingSpaceIndefiniteSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSpaceResponse response = parkingSpaceService.pauseParkingSpaceIndefinitely(100L, 1L);

        assertEquals(ParkingSpaceStatus.PAUSED, response.getStatus());
        assertNull(response.getPauseUntil());
    }

    @Test
    void pauseParkingSpaceInPastThrowsInvalidPauseRequest() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        PauseParkingSpaceRequest request = PauseParkingSpaceRequest.custom(past);

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));

        assertThrows(InvalidPauseRequestException.class, () ->
                parkingSpaceService.pauseParkingSpace(100L, 1L, request));
    }

    @Test
    void resumeParkingSpaceSuccess() {
        sampleSpace.setStatus(ParkingSpaceStatus.PAUSED);
        sampleSpace.setPauseUntil(LocalDateTime.now().plusHours(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));
        when(parkingSpaceRepository.save(any(ParkingSpace.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSpaceResponse response = parkingSpaceService.resumeParkingSpace(100L, 1L);

        assertEquals(ParkingSpaceStatus.ACTIVE, response.getStatus());
        assertNull(response.getPauseUntil());
    }

    @Test
    void pauseRemovedSpaceThrowsException() {
        sampleSpace.setStatus(ParkingSpaceStatus.REMOVED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(parkingSpaceRepository.findById(100L)).thenReturn(Optional.of(sampleSpace));

        assertThrows(InvalidParkingSpaceException.class, () ->
                parkingSpaceService.pauseParkingSpace(100L, 1L, 8));
    }
}
