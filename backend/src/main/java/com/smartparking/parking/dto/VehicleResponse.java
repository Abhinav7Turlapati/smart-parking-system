package com.smartparking.parking.dto;

import com.smartparking.parking.entity.Vehicle;
import com.smartparking.parking.entity.VehicleCategory;

import java.time.LocalDateTime;

public class VehicleResponse {

    private Long id;
    private Long userId;
    private Long brandId;
    private String brandName;
    private Long modelId;
    private String modelName;
    private VehicleCategory category;
    private String registrationNumber;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VehicleResponse() {
    }

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        if (vehicle.getUser() != null) {
            response.setUserId(vehicle.getUser().getId());
        }
        if (vehicle.getVehicleModel() != null) {
            response.setModelId(vehicle.getVehicleModel().getId());
            response.setModelName(vehicle.getVehicleModel().getName());
            response.setCategory(vehicle.getVehicleModel().getCategory());
            if (vehicle.getVehicleModel().getBrand() != null) {
                response.setBrandId(vehicle.getVehicleModel().getBrand().getId());
                response.setBrandName(vehicle.getVehicleModel().getBrand().getName());
            }
        }
        response.setRegistrationNumber(vehicle.getRegistrationNumber());
        response.setActive(vehicle.isActive());
        response.setCreatedAt(vehicle.getCreatedAt());
        response.setUpdatedAt(vehicle.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
