package com.smartparking.parking.config;

import com.smartparking.parking.entity.VehicleBrand;
import com.smartparking.parking.entity.VehicleCategory;
import com.smartparking.parking.entity.VehicleModel;
import com.smartparking.parking.repository.VehicleBrandRepository;
import com.smartparking.parking.repository.VehicleModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class VehicleCatalogSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VehicleCatalogSeeder.class);

    private final VehicleBrandRepository brandRepository;
    private final VehicleModelRepository modelRepository;

    public VehicleCatalogSeeder(VehicleBrandRepository brandRepository, VehicleModelRepository modelRepository) {
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding Vehicle Catalog...");

        seedCatalog();

        log.info("Vehicle Catalog seeding completed successfully.");
    }

    public void seedCatalog() {
        // 1. Maruti Suzuki
        seedBrandWithModels("Maruti Suzuki", List.of(
                new ModelSeed("Alto", VehicleCategory.HATCHBACK),
                new ModelSeed("Wagon R", VehicleCategory.HATCHBACK),
                new ModelSeed("Swift", VehicleCategory.HATCHBACK),
                new ModelSeed("Baleno", VehicleCategory.HATCHBACK),
                new ModelSeed("Dzire", VehicleCategory.SEDAN),
                new ModelSeed("Ciaz", VehicleCategory.SEDAN),
                new ModelSeed("Brezza", VehicleCategory.SUV),
                new ModelSeed("Grand Vitara", VehicleCategory.SUV),
                new ModelSeed("Ertiga", VehicleCategory.SUV),
                new ModelSeed("Invicto", VehicleCategory.LARGE_SUV)
        ));

        // 2. Hyundai
        seedBrandWithModels("Hyundai", List.of(
                new ModelSeed("Grand i10 Nios", VehicleCategory.HATCHBACK),
                new ModelSeed("i20", VehicleCategory.HATCHBACK),
                new ModelSeed("Aura", VehicleCategory.SEDAN),
                new ModelSeed("Verna", VehicleCategory.SEDAN),
                new ModelSeed("Venue", VehicleCategory.SUV),
                new ModelSeed("Creta", VehicleCategory.SUV),
                new ModelSeed("Alcazar", VehicleCategory.SUV),
                new ModelSeed("Tucson", VehicleCategory.SUV)
        ));

        // 3. Tata
        seedBrandWithModels("Tata", List.of(
                new ModelSeed("Tiago", VehicleCategory.HATCHBACK),
                new ModelSeed("Altroz", VehicleCategory.HATCHBACK),
                new ModelSeed("Tigor", VehicleCategory.SEDAN),
                new ModelSeed("Punch", VehicleCategory.SUV),
                new ModelSeed("Nexon", VehicleCategory.SUV),
                new ModelSeed("Harrier", VehicleCategory.SUV),
                new ModelSeed("Safari", VehicleCategory.LARGE_SUV)
        ));

        // 4. Mahindra
        seedBrandWithModels("Mahindra", List.of(
                new ModelSeed("Thar", VehicleCategory.SUV),
                new ModelSeed("Bolero", VehicleCategory.SUV),
                new ModelSeed("XUV300", VehicleCategory.SUV),
                new ModelSeed("XUV700", VehicleCategory.SUV),
                new ModelSeed("Scorpio Classic", VehicleCategory.LARGE_SUV),
                new ModelSeed("Scorpio-N", VehicleCategory.LARGE_SUV)
        ));

        // 5. Toyota
        seedBrandWithModels("Toyota", List.of(
                new ModelSeed("Glanza", VehicleCategory.HATCHBACK),
                new ModelSeed("Urban Cruiser Hyryder", VehicleCategory.SUV),
                new ModelSeed("Camry", VehicleCategory.SEDAN),
                new ModelSeed("Innova Crysta", VehicleCategory.LARGE_SUV),
                new ModelSeed("Innova Hycross", VehicleCategory.LARGE_SUV),
                new ModelSeed("Fortuner", VehicleCategory.LARGE_SUV)
        ));

        // 6. Kia
        seedBrandWithModels("Kia", List.of(
                new ModelSeed("Sonet", VehicleCategory.SUV),
                new ModelSeed("Seltos", VehicleCategory.SUV),
                new ModelSeed("Carens", VehicleCategory.SUV),
                new ModelSeed("EV6", VehicleCategory.SUV),
                new ModelSeed("Carnival", VehicleCategory.LARGE_SUV)
        ));

        // 7. Honda
        seedBrandWithModels("Honda", List.of(
                new ModelSeed("Amaze", VehicleCategory.SEDAN),
                new ModelSeed("City", VehicleCategory.SEDAN),
                new ModelSeed("Elevate", VehicleCategory.SUV)
        ));

        // 8. Volkswagen
        seedBrandWithModels("Volkswagen", List.of(
                new ModelSeed("Polo", VehicleCategory.HATCHBACK),
                new ModelSeed("Virtus", VehicleCategory.SEDAN),
                new ModelSeed("Taigun", VehicleCategory.SUV),
                new ModelSeed("Tiguan", VehicleCategory.SUV)
        ));

        // 9. Skoda
        seedBrandWithModels("Skoda", List.of(
                new ModelSeed("Slavia", VehicleCategory.SEDAN),
                new ModelSeed("Octavia", VehicleCategory.SEDAN),
                new ModelSeed("Superb", VehicleCategory.SEDAN),
                new ModelSeed("Kushaq", VehicleCategory.SUV),
                new ModelSeed("Kodiaq", VehicleCategory.LARGE_SUV)
        ));

        // 10. Renault
        seedBrandWithModels("Renault", List.of(
                new ModelSeed("Kwid", VehicleCategory.HATCHBACK),
                new ModelSeed("Triber", VehicleCategory.HATCHBACK),
                new ModelSeed("Kiger", VehicleCategory.SUV),
                new ModelSeed("Duster", VehicleCategory.SUV)
        ));

        // 11. Nissan
        seedBrandWithModels("Nissan", List.of(
                new ModelSeed("Magnite", VehicleCategory.SUV),
                new ModelSeed("X-Trail", VehicleCategory.LARGE_SUV)
        ));

        // 12. MG
        seedBrandWithModels("MG", List.of(
                new ModelSeed("Comet EV", VehicleCategory.HATCHBACK),
                new ModelSeed("Astor", VehicleCategory.SUV),
                new ModelSeed("Hector", VehicleCategory.SUV),
                new ModelSeed("ZS EV", VehicleCategory.SUV),
                new ModelSeed("Gloster", VehicleCategory.LARGE_SUV)
        ));

        // 13. Jeep
        seedBrandWithModels("Jeep", List.of(
                new ModelSeed("Compass", VehicleCategory.SUV),
                new ModelSeed("Meridian", VehicleCategory.LARGE_SUV),
                new ModelSeed("Wrangler", VehicleCategory.LARGE_SUV),
                new ModelSeed("Grand Cherokee", VehicleCategory.LARGE_SUV)
        ));

        // 14. Ford
        seedBrandWithModels("Ford", List.of(
                new ModelSeed("Figo", VehicleCategory.HATCHBACK),
                new ModelSeed("Aspire", VehicleCategory.SEDAN),
                new ModelSeed("EcoSport", VehicleCategory.SUV),
                new ModelSeed("Endeavour", VehicleCategory.LARGE_SUV)
        ));
    }

    private void seedBrandWithModels(String brandName, List<ModelSeed> models) {
        VehicleBrand brand = brandRepository.findByNameIgnoreCase(brandName)
                .orElseGet(() -> brandRepository.save(new VehicleBrand(brandName)));

        for (ModelSeed modelSeed : models) {
            boolean exists = modelRepository.existsByBrandIdAndNameIgnoreCase(brand.getId(), modelSeed.name());
            if (!exists) {
                modelRepository.save(new VehicleModel(modelSeed.name(), brand, modelSeed.category()));
            }
        }
    }

    public record ModelSeed(String name, VehicleCategory category) {}
}
