package com.smartparking.parking.controller;

import com.smartparking.parking.dto.CreateParkingSpaceRequest;
import com.smartparking.parking.dto.ParkingSpaceResponse;
import com.smartparking.parking.dto.PauseParkingSpaceRequest;
import com.smartparking.parking.dto.UpdateParkingSpaceRequest;
import com.smartparking.parking.exception.AccessDeniedException;
import com.smartparking.parking.exception.InvalidPauseRequestException;
import com.smartparking.parking.security.UserPrincipal;
import com.smartparking.parking.service.ParkingSpaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-spaces")
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    public ParkingSpaceController(ParkingSpaceService parkingSpaceService) {
        this.parkingSpaceService = parkingSpaceService;
    }

    /**
     * 1. Create a parking space.
     * POST /api/parking-spaces
     * - Only authenticated USER can create a parking space.
     * - ADMIN must not create normal user parking listings.
     * - Status: 201 Created
     */
    @PostMapping
    public ResponseEntity<ParkingSpaceResponse> createParkingSpace(
            @Valid @RequestBody CreateParkingSpaceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        ParkingSpaceResponse response = parkingSpaceService.createParkingSpace(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. Get my parking spaces.
     * GET /api/parking-spaces/my
     * - Authenticated USER only.
     * - Returns parking spaces belonging to the logged-in user.
     * - Status: 200 OK
     */
    @GetMapping("/my")
    public ResponseEntity<List<ParkingSpaceResponse>> getMyParkingSpaces(
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        List<ParkingSpaceResponse> responses = parkingSpaceService.getMyParkingSpaces(principal.getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 3. Get parking space by ID.
     * GET /api/parking-spaces/{id}
     * - Authenticated USER/ADMIN can view.
     * - REMOVED spaces return 404 Not Found.
     * - Status: 200 OK or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpaceResponse> getParkingSpaceById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        ParkingSpaceResponse response = parkingSpaceService.getParkingSpaceById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. Update parking space.
     * PUT /api/parking-spaces/{id}
     * - Authenticated USER only (owner only).
     * - Only permitted fields allowed.
     * - Status: 200 OK (or 400, 403, 404)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ParkingSpaceResponse> updateParkingSpace(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateParkingSpaceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        ParkingSpaceResponse response = parkingSpaceService.updateParkingSpace(id, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 5. Remove parking space.
     * DELETE /api/parking-spaces/{id}
     * - Authenticated USER only (owner only).
     * - Soft removal (status = REMOVED).
     * - Status: 200 OK
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ParkingSpaceResponse> removeParkingSpace(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        ParkingSpaceResponse response = parkingSpaceService.removeParkingSpace(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 6. Pause parking space.
     * POST /api/parking-spaces/{id}/pause
     * - Authenticated USER only (owner only).
     * - Supports 8h, 24h, custom date-time, indefinite pause.
     * - Status: 200 OK (or 400, 403, 404)
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<ParkingSpaceResponse> pauseParkingSpace(
            @PathVariable("id") Long id,
            @RequestBody(required = false) PauseParkingSpaceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        if (request == null) {
            throw new InvalidPauseRequestException("Pause request body is required");
        }
        ParkingSpaceResponse response = parkingSpaceService.pauseParkingSpace(id, principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 7. Resume parking space.
     * POST /api/parking-spaces/{id}/resume
     * - Authenticated USER only (owner only).
     * - Sets space back to ACTIVE.
     * - Status: 200 OK (or 403, 404)
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<ParkingSpaceResponse> resumeParkingSpace(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateAuthenticated(principal);
        ParkingSpaceResponse response = parkingSpaceService.resumeParkingSpace(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    private void validateAuthenticated(UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required");
        }
    }
}
