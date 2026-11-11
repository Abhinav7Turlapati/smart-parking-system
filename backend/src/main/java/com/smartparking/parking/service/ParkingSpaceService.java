package com.smartparking.parking.service;

import com.smartparking.parking.dto.CreateParkingSpaceRequest;
import com.smartparking.parking.dto.ParkingSpaceResponse;
import com.smartparking.parking.dto.PauseParkingSpaceRequest;
import com.smartparking.parking.dto.UpdateParkingSpaceRequest;
import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.entity.ParkingSpaceStatus;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.exception.AccessDeniedException;
import com.smartparking.parking.exception.InvalidParkingSpaceException;
import com.smartparking.parking.exception.InvalidPauseRequestException;
import com.smartparking.parking.exception.ResourceNotFoundException;
import com.smartparking.parking.repository.ParkingSpaceRepository;
import com.smartparking.parking.repository.UserRepository;
import com.smartparking.parking.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ParkingSpaceService {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final UserRepository userRepository;

    public ParkingSpaceService(ParkingSpaceRepository parkingSpaceRepository, UserRepository userRepository) {
        this.parkingSpaceRepository = parkingSpaceRepository;
        this.userRepository = userRepository;
    }

    /**
     * 1. Create a new parking space listed by the authenticated user.
     * Owner is strictly derived from the authenticated user; request owner fields are never trusted.
     * Admins are not allowed to create user parking listings.
     */
    public ParkingSpaceResponse createParkingSpace(CreateParkingSpaceRequest request, Long currentUserId) {
        if (request == null) {
            throw new InvalidParkingSpaceException("Create parking space request body cannot be null");
        }

        User owner = validateAndGetNormalUser(currentUserId);

        ParkingSpace space = new ParkingSpace();
        space.setOwner(owner);
        space.setDescription(request.getDescription());
        space.setHourlyPrice(request.getHourlyPrice());
        space.setAddress(request.getAddress());
        space.setLatitude(request.getLatitude());
        space.setLongitude(request.getLongitude());
        space.setMaximumVehicleCategory(request.getMaximumVehicleCategory());
        space.setPhoto1(request.getPhoto1());
        space.setPhoto2(request.getPhoto2());
        space.setStatus(ParkingSpaceStatus.ACTIVE);
        space.setPauseUntil(null);

        ParkingSpace saved = parkingSpaceRepository.save(space);
        return ParkingSpaceResponse.fromEntity(saved);
    }

    public ParkingSpaceResponse createParkingSpace(CreateParkingSpaceRequest request, UserPrincipal principal) {
        validatePrincipal(principal);
        return createParkingSpace(request, principal.getId());
    }

    /**
     * 2. Get all parking spaces listed by the authenticated user.
     * Admins cannot manage normal user parking listings.
     * Automatically handles expired temporary pauses by activating the space.
     */
    public List<ParkingSpaceResponse> getMyParkingSpaces(Long currentUserId) {
        validateAndGetNormalUser(currentUserId);

        List<ParkingSpace> spaces = parkingSpaceRepository.findByOwnerId(currentUserId);
        return spaces.stream()
                .map(this::refreshPauseStatusIfNeeded)
                .map(ParkingSpaceResponse::fromEntity)
                .toList();
    }

    public List<ParkingSpaceResponse> getMyParkingSpaces(UserPrincipal principal) {
        validatePrincipal(principal);
        return getMyParkingSpaces(principal.getId());
    }

    /**
     * 3. Get a parking space by ID.
     * If a temporary pause on the space has expired, refreshes status to ACTIVE.
     */
    public ParkingSpaceResponse getParkingSpaceById(Long spaceId) {
        if (spaceId == null) {
            throw new ResourceNotFoundException("Parking space ID cannot be null");
        }

        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with id: " + spaceId));

        if (space.getStatus() == ParkingSpaceStatus.REMOVED) {
            throw new ResourceNotFoundException("Parking space not found with id: " + spaceId);
        }

        space = refreshPauseStatusIfNeeded(space);
        return ParkingSpaceResponse.fromEntity(space);
    }

    /**
     * Get a parking space by ID with ownership verification.
     */
    public ParkingSpaceResponse getParkingSpaceById(Long spaceId, Long currentUserId) {
        User user = validateAndGetNormalUser(currentUserId);

        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with id: " + spaceId));

        if (space.getStatus() == ParkingSpaceStatus.REMOVED) {
            throw new ResourceNotFoundException("Parking space not found with id: " + spaceId);
        }

        if (!space.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this parking space");
        }

        space = refreshPauseStatusIfNeeded(space);
        return ParkingSpaceResponse.fromEntity(space);
    }

    /**
     * 4. Update an existing parking space.
     * - Only the owner can update.
     * - Admins cannot manage user listings.
     * - Cannot update a REMOVED parking space.
     * - Client cannot update id, owner, status, createdAt, updatedAt.
     */
    public ParkingSpaceResponse updateParkingSpace(Long spaceId, UpdateParkingSpaceRequest request, Long currentUserId) {
        if (spaceId == null) {
            throw new ResourceNotFoundException("Parking space ID cannot be null");
        }
        if (request == null) {
            throw new InvalidParkingSpaceException("Update parking space request body cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);
        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with id: " + spaceId));

        verifyOwnership(space, user);
        verifyNotRemoved(space);

        space = refreshPauseStatusIfNeeded(space);

        space.setDescription(request.getDescription());
        space.setHourlyPrice(request.getHourlyPrice());
        space.setAddress(request.getAddress());
        space.setLatitude(request.getLatitude());
        space.setLongitude(request.getLongitude());
        space.setMaximumVehicleCategory(request.getMaximumVehicleCategory());
        space.setPhoto1(request.getPhoto1());
        space.setPhoto2(request.getPhoto2());

        ParkingSpace saved = parkingSpaceRepository.save(space);
        return ParkingSpaceResponse.fromEntity(saved);
    }

    public ParkingSpaceResponse updateParkingSpace(Long spaceId, UpdateParkingSpaceRequest request, UserPrincipal principal) {
        validatePrincipal(principal);
        return updateParkingSpace(spaceId, request, principal.getId());
    }

    /**
     * 5. Remove a parking space (Soft Removal).
     * - Sets status to REMOVED.
     * - Row is preserved in database for history.
     * - Cannot remove a space that does not belong to the user.
     * - Cannot remove an already REMOVED space.
     */
    public ParkingSpaceResponse removeParkingSpace(Long spaceId, Long currentUserId) {
        if (spaceId == null) {
            throw new ResourceNotFoundException("Parking space ID cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);
        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with id: " + spaceId));

        verifyOwnership(space, user);

        if (space.getStatus() == ParkingSpaceStatus.REMOVED) {
            throw new InvalidParkingSpaceException("Parking space is already removed");
        }

        space.setStatus(ParkingSpaceStatus.REMOVED);
        space.setPauseUntil(null);

        ParkingSpace saved = parkingSpaceRepository.save(space);
        return ParkingSpaceResponse.fromEntity(saved);
    }

    public ParkingSpaceResponse removeParkingSpace(Long spaceId, UserPrincipal principal) {
        validatePrincipal(principal);
        return removeParkingSpace(spaceId, principal.getId());
    }

    /**
     * 6. Pause a parking space with options:
     * - 8 hours
     * - 24 hours
     * - Custom duration/date-time
     * - Indefinite (until manually resumed)
     */
    public ParkingSpaceResponse pauseParkingSpace(Long spaceId, Long currentUserId, PauseParkingSpaceRequest request) {
        if (spaceId == null) {
            throw new ResourceNotFoundException("Parking space ID cannot be null");
        }
        if (request == null) {
            throw new InvalidPauseRequestException("Pause request cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);
        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with id: " + spaceId));

        verifyOwnership(space, user);
        verifyNotRemoved(space);

        LocalDateTime pauseUntil = resolvePauseUntil(request);

        space.setStatus(ParkingSpaceStatus.PAUSED);
        space.setPauseUntil(pauseUntil);

        ParkingSpace saved = parkingSpaceRepository.save(space);
        return ParkingSpaceResponse.fromEntity(saved);
    }

    public ParkingSpaceResponse pauseParkingSpace(Long spaceId, UserPrincipal principal, PauseParkingSpaceRequest request) {
        validatePrincipal(principal);
        return pauseParkingSpace(spaceId, principal.getId(), request);
    }

    /**
     * Convenience method to pause for a specified number of hours (e.g. 8 or 24 hours).
     */
    public ParkingSpaceResponse pauseParkingSpace(Long spaceId, Long currentUserId, Integer hours) {
        if (hours == null || hours <= 0) {
            throw new InvalidPauseRequestException("Pause hours must be greater than 0");
        }
        return pauseParkingSpace(spaceId, currentUserId, new PauseParkingSpaceRequest(hours));
    }

    /**
     * Convenience method to pause until a custom date-time.
     */
    public ParkingSpaceResponse pauseParkingSpace(Long spaceId, Long currentUserId, LocalDateTime pauseUntil) {
        if (pauseUntil == null || !pauseUntil.isAfter(LocalDateTime.now())) {
            throw new InvalidPauseRequestException("Pause date-time must be in the future");
        }
        return pauseParkingSpace(spaceId, currentUserId, new PauseParkingSpaceRequest(pauseUntil));
    }

    /**
     * Convenience method to pause indefinitely until manually resumed.
     */
    public ParkingSpaceResponse pauseParkingSpaceIndefinitely(Long spaceId, Long currentUserId) {
        return pauseParkingSpace(spaceId, currentUserId, PauseParkingSpaceRequest.indefinite());
    }

    /**
     * Resume a paused parking space back to ACTIVE.
     */
    public ParkingSpaceResponse resumeParkingSpace(Long spaceId, Long currentUserId) {
        if (spaceId == null) {
            throw new ResourceNotFoundException("Parking space ID cannot be null");
        }

        User user = validateAndGetNormalUser(currentUserId);
        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found with id: " + spaceId));

        verifyOwnership(space, user);
        verifyNotRemoved(space);

        space.setStatus(ParkingSpaceStatus.ACTIVE);
        space.setPauseUntil(null);

        ParkingSpace saved = parkingSpaceRepository.save(space);
        return ParkingSpaceResponse.fromEntity(saved);
    }

    public ParkingSpaceResponse resumeParkingSpace(Long spaceId, UserPrincipal principal) {
        validatePrincipal(principal);
        return resumeParkingSpace(spaceId, principal.getId());
    }

    // Helper validation and status refresh methods

    private User validateAndGetNormalUser(Long userId) {
        if (userId == null) {
            throw new ResourceNotFoundException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException("Admins are not allowed to create or manage user parking listings");
        }

        return user;
    }

    private void validatePrincipal(UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("User must be authenticated");
        }
    }

    private void verifyOwnership(ParkingSpace space, User user) {
        if (space.getOwner() == null || !space.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this parking space");
        }
    }

    private void verifyNotRemoved(ParkingSpace space) {
        if (space.getStatus() == ParkingSpaceStatus.REMOVED) {
            throw new InvalidParkingSpaceException("Cannot modify a removed parking space");
        }
    }

    private LocalDateTime resolvePauseUntil(PauseParkingSpaceRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Check option string if provided
        if (request.getOption() != null) {
            String opt = request.getOption().trim().toUpperCase();
            switch (opt) {
                case "8_HOURS":
                case "8":
                case "EIGHT_HOURS":
                    return now.plusHours(8);
                case "24_HOURS":
                case "24":
                case "TWENTY_FOUR_HOURS":
                    return now.plusHours(24);
                case "INDEFINITE":
                case "MANUAL":
                case "UNTIL_MANUALLY_RESUMED":
                    return null;
                case "CUSTOM":
                    // fall through to check pauseUntil or hours
                    break;
                default:
                    throw new InvalidPauseRequestException("Unknown pause option: " + request.getOption());
            }
        }

        // 2. Check indefinite flag
        if (Boolean.TRUE.equals(request.getIndefinite())) {
            return null;
        }

        // 3. Check hours
        if (request.getHours() != null) {
            if (request.getHours() <= 0) {
                throw new InvalidPauseRequestException("Pause hours must be greater than 0");
            }
            return now.plusHours(request.getHours());
        }

        // 4. Check custom pauseUntil date-time
        if (request.getPauseUntil() != null) {
            if (!request.getPauseUntil().isAfter(now)) {
                throw new InvalidPauseRequestException("Pause date-time must be in the future");
            }
            return request.getPauseUntil();
        }

        throw new InvalidPauseRequestException("Must specify a valid pause duration (hours, pauseUntil date-time, or indefinite)");
    }

    /**
     * Automatically treats a temporarily paused space as active once the pause duration has expired.
     * Updates the database record lazily if expired.
     */
    private ParkingSpace refreshPauseStatusIfNeeded(ParkingSpace space) {
        if (space.getStatus() == ParkingSpaceStatus.PAUSED
                && space.getPauseUntil() != null
                && !space.getPauseUntil().isAfter(LocalDateTime.now())) {
            space.setStatus(ParkingSpaceStatus.ACTIVE);
            space.setPauseUntil(null);
            return parkingSpaceRepository.save(space);
        }
        return space;
    }
}
