package com.smartparking.parking.entity;

public enum VehicleCategory {
    HATCHBACK(1),
    SEDAN(2),
    SUV(3),
    LARGE_SUV(4);

    private final int level;

    VehicleCategory(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Checks if a vehicle of the requested category can fit into a parking space
     * configured for this maximum category.
     *
     * Hierarchy: HATCHBACK (1) < SEDAN (2) < SUV (3) < LARGE_SUV (4)
     * A space can accommodate any vehicle whose category level is less than or equal to this maximum level.
     */
    public boolean canAccommodate(VehicleCategory vehicleCategory) {
        if (vehicleCategory == null) {
            return false;
        }
        return this.level >= vehicleCategory.level;
    }
}
