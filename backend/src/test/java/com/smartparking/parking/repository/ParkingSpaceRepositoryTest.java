package com.smartparking.parking.repository;

import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.entity.ParkingSpaceStatus;
import com.smartparking.parking.entity.User;
import com.smartparking.parking.entity.VehicleCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ParkingSpaceRepositoryTest {

    @Autowired
    private ParkingSpaceRepository parkingSpaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void repositoryQueriesWorkCorrectly() {
        String uniqueEmail = "repo-test-" + UUID.randomUUID() + "@example.com";
        User user = new User("Repo Test User", uniqueEmail, "password123", "USER");
        user = userRepository.save(user);

        ParkingSpace space1 = new ParkingSpace(
                user,
                "Space 1",
                new BigDecimal("45.00"),
                "10 MG Road",
                12.97,
                77.59,
                VehicleCategory.SEDAN,
                "photo1.jpg",
                null,
                ParkingSpaceStatus.ACTIVE
        );
        parkingSpaceRepository.save(space1);

        ParkingSpace space2 = new ParkingSpace(
                user,
                "Space 2",
                new BigDecimal("60.00"),
                "20 MG Road",
                12.98,
                77.60,
                VehicleCategory.SUV,
                "photo2.jpg",
                null,
                ParkingSpaceStatus.REMOVED
        );
        parkingSpaceRepository.save(space2);

        // findByOwnerId
        List<ParkingSpace> userSpaces = parkingSpaceRepository.findByOwnerId(user.getId());
        assertEquals(2, userSpaces.size());

        // findByOwnerIdAndStatusNot
        List<ParkingSpace> nonRemovedSpaces = parkingSpaceRepository.findByOwnerIdAndStatusNot(user.getId(), ParkingSpaceStatus.REMOVED);
        assertEquals(1, nonRemovedSpaces.size());
        assertEquals("Space 1", nonRemovedSpaces.get(0).getDescription());

        // existsByIdAndOwnerId
        assertTrue(parkingSpaceRepository.existsByIdAndOwnerId(space1.getId(), user.getId()));
        assertFalse(parkingSpaceRepository.existsByIdAndOwnerId(space1.getId(), 99999L));

        // findByIdAndOwnerId
        Optional<ParkingSpace> found = parkingSpaceRepository.findByIdAndOwnerId(space1.getId(), user.getId());
        assertTrue(found.isPresent());
        assertEquals(space1.getId(), found.get().getId());
    }
}
