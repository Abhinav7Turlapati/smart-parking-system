package com.smartparking.parking.entity;

import com.smartparking.parking.config.VehicleCatalogSeeder;
import com.smartparking.parking.repository.UserRepository;
import com.smartparking.parking.repository.VehicleBrandRepository;
import com.smartparking.parking.repository.VehicleModelRepository;
import com.smartparking.parking.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VehicleEntityAndCatalogTest {

    @Autowired
    private VehicleBrandRepository brandRepository;

    @Autowired
    private VehicleModelRepository modelRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleCatalogSeeder seeder;

    @Test
    void catalogIsSeededWithPopularIndianBrandsAndModels() {
        // Seeder runs on startup via CommandLineRunner, but verify count and contents
        List<VehicleBrand> brands = brandRepository.findAllByOrderByNameAsc();
        assertTrue(brands.size() >= 14, "Expected at least 14 brands seeded");

        // Verify specific popular Indian brands
        assertTrue(brandRepository.existsByNameIgnoreCase("Maruti Suzuki"));
        assertTrue(brandRepository.existsByNameIgnoreCase("Hyundai"));
        assertTrue(brandRepository.existsByNameIgnoreCase("Tata"));
        assertTrue(brandRepository.existsByNameIgnoreCase("Mahindra"));
        assertTrue(brandRepository.existsByNameIgnoreCase("Toyota"));
        assertTrue(brandRepository.existsByNameIgnoreCase("Kia"));

        // Verify models and categories
        VehicleBrand tata = brandRepository.findByNameIgnoreCase("Tata").orElseThrow();
        Optional<VehicleModel> nexon = modelRepository.findByBrandIdAndNameIgnoreCase(tata.getId(), "Nexon");
        assertTrue(nexon.isPresent());
        assertEquals(VehicleCategory.SUV, nexon.get().getCategory());

        Optional<VehicleModel> safari = modelRepository.findByBrandIdAndNameIgnoreCase(tata.getId(), "Safari");
        assertTrue(safari.isPresent());
        assertEquals(VehicleCategory.LARGE_SUV, safari.get().getCategory());

        Optional<VehicleModel> tiago = modelRepository.findByBrandIdAndNameIgnoreCase(tata.getId(), "Tiago");
        assertTrue(tiago.isPresent());
        assertEquals(VehicleCategory.HATCHBACK, tiago.get().getCategory());

        Optional<VehicleModel> tigor = modelRepository.findByBrandIdAndNameIgnoreCase(tata.getId(), "Tigor");
        assertTrue(tigor.isPresent());
        assertEquals(VehicleCategory.SEDAN, tigor.get().getCategory());

        // Verify all 4 categories have seeded models
        for (VehicleCategory cat : VehicleCategory.values()) {
            assertFalse(modelRepository.findByCategory(cat).isEmpty(),
                    "Expected models seeded for category: " + cat);
        }
    }

    @Test
    void seederIsIdempotentAndDoesNotDuplicateRecords() {
        long brandCountBefore = brandRepository.count();
        long modelCountBefore = modelRepository.count();

        // Run seeder again
        seeder.seedCatalog();

        assertEquals(brandCountBefore, brandRepository.count(), "Brand count should remain unchanged after re-run");
        assertEquals(modelCountBefore, modelRepository.count(), "Model count should remain unchanged after re-run");
    }

    @Test
    void vehicleEntityPersistenceAndCategoryDerivation() {
        User user = userRepository.save(new User("Vehicle Owner", "vo-" + UUID.randomUUID() + "@example.com", "pass", "USER"));

        VehicleBrand hyundai = brandRepository.findByNameIgnoreCase("Hyundai").orElseThrow();
        VehicleModel verna = modelRepository.findByBrandIdAndNameIgnoreCase(hyundai.getId(), "Verna").orElseThrow();

        Vehicle vehicle = new Vehicle(user, verna, "ka 05 mh 9999");
        Vehicle saved = vehicleRepository.save(vehicle);

        assertNotNull(saved.getId());
        assertEquals(user.getId(), saved.getUser().getId());
        assertEquals(verna.getId(), saved.getVehicleModel().getId());

        // Normalized to uppercase
        assertEquals("KA 05 MH 9999", saved.getRegistrationNumber());

        // Category is derived from VehicleModel
        assertEquals(VehicleCategory.SEDAN, saved.getCategory());

        // Default active = true
        assertTrue(saved.isActive());
        assertTrue(saved.getActive());

        // Timestamps populated
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void vehicleSoftDeleteLeavesRecordInDatabase() {
        User user = userRepository.save(new User("Soft Delete Owner", "sdo-" + UUID.randomUUID() + "@example.com", "pass", "USER"));
        VehicleBrand maruti = brandRepository.findByNameIgnoreCase("Maruti Suzuki").orElseThrow();
        VehicleModel swift = modelRepository.findByBrandIdAndNameIgnoreCase(maruti.getId(), "Swift").orElseThrow();

        Vehicle vehicle = new Vehicle(user, swift, "MH-12-AB-1234");
        Vehicle saved = vehicleRepository.save(vehicle);

        // Perform soft delete
        saved.setActive(false);
        vehicleRepository.save(saved);

        // Record still exists in DB
        Optional<Vehicle> found = vehicleRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().isActive());

        // Active query returns empty, but user query returns all
        assertTrue(vehicleRepository.findByUserIdAndActiveTrue(user.getId()).isEmpty());
        assertEquals(1, vehicleRepository.findByUserId(user.getId()).size());
    }

    @Test
    void duplicateRegistrationNumberForSameUserIsPrevented() {
        User user = userRepository.save(new User("Dup Owner", "dup-" + UUID.randomUUID() + "@example.com", "pass", "USER"));
        VehicleBrand toyota = brandRepository.findByNameIgnoreCase("Toyota").orElseThrow();
        VehicleModel fortuner = modelRepository.findByBrandIdAndNameIgnoreCase(toyota.getId(), "Fortuner").orElseThrow();

        Vehicle v1 = new Vehicle(user, fortuner, "DL 01 AA 0001");
        vehicleRepository.saveAndFlush(v1);

        Vehicle v2 = new Vehicle(user, fortuner, "dl 01 aa 0001"); // same normalized registration number

        assertThrows(DataIntegrityViolationException.class, () -> {
            vehicleRepository.saveAndFlush(v2);
        });
    }
}
