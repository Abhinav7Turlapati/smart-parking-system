package com.smartparking.parking.dto;

import com.smartparking.parking.entity.VehicleCategory;

import java.math.BigDecimal;

/**
 * Alias DTO for CreateParkingSpaceRequest.
 */
public class CreateParkingSpaceDto extends CreateParkingSpaceRequest {

    public CreateParkingSpaceDto() {
        super();
    }

    public CreateParkingSpaceDto(String description, BigDecimal hourlyPrice, String address,
                                 Double latitude, Double longitude, VehicleCategory maximumVehicleCategory,
                                 String photo1, String photo2) {
        super(description, hourlyPrice, address, latitude, longitude, maximumVehicleCategory, photo1, photo2);
    }
}
