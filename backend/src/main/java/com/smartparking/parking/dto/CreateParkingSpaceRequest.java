package com.smartparking.parking.dto;

import com.smartparking.parking.entity.VehicleCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreateParkingSpaceRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Hourly price is required")
    @Positive(message = "Hourly price must be greater than 0")
    private BigDecimal hourlyPrice;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @NotNull(message = "Maximum vehicle category is required")
    private VehicleCategory maximumVehicleCategory;

    @NotBlank(message = "photo1 is required")
    private String photo1;

    private String photo2;

    public CreateParkingSpaceRequest() {
    }

    public CreateParkingSpaceRequest(String description, BigDecimal hourlyPrice, String address,
                                     Double latitude, Double longitude, VehicleCategory maximumVehicleCategory,
                                     String photo1, String photo2) {
        this.description = description;
        this.hourlyPrice = hourlyPrice;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maximumVehicleCategory = maximumVehicleCategory;
        this.photo1 = photo1;
        this.photo2 = photo2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getHourlyPrice() {
        return hourlyPrice;
    }

    public void setHourlyPrice(BigDecimal hourlyPrice) {
        this.hourlyPrice = hourlyPrice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public VehicleCategory getMaximumVehicleCategory() {
        return maximumVehicleCategory;
    }

    public void setMaximumVehicleCategory(VehicleCategory maximumVehicleCategory) {
        this.maximumVehicleCategory = maximumVehicleCategory;
    }

    public String getPhoto1() {
        return photo1;
    }

    public void setPhoto1(String photo1) {
        this.photo1 = photo1;
    }

    public String getPhoto2() {
        return photo2;
    }

    public void setPhoto2(String photo2) {
        this.photo2 = photo2;
    }
}
