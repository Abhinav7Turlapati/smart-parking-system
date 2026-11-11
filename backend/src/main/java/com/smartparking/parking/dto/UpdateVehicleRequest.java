package com.smartparking.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateVehicleRequest {

    @NotNull(message = "Vehicle model ID is required")
    private Long vehicleModelId;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    public UpdateVehicleRequest() {
    }

    public UpdateVehicleRequest(Long vehicleModelId, String registrationNumber) {
        this.vehicleModelId = vehicleModelId;
        this.setRegistrationNumber(registrationNumber);
    }

    public Long getVehicleModelId() {
        return vehicleModelId;
    }

    public void setVehicleModelId(Long vehicleModelId) {
        this.vehicleModelId = vehicleModelId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        if (registrationNumber != null) {
            this.registrationNumber = registrationNumber.trim().toUpperCase();
        } else {
            this.registrationNumber = null;
        }
    }
}
