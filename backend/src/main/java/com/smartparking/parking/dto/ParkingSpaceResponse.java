package com.smartparking.parking.dto;

import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.entity.ParkingSpaceStatus;
import com.smartparking.parking.entity.VehicleCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParkingSpaceResponse {

    private Long id;
    private Long listedBy;
    private String description;
    private BigDecimal hourlyPrice;
    private String address;
    private Double latitude;
    private Double longitude;
    private VehicleCategory maximumVehicleCategory;
    private List<VehicleCategory> compatibleVehicleCategories;
    private String photo1;
    private String photo2;
    private ParkingSpaceStatus status;
    private LocalDateTime pauseUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ParkingSpaceResponse() {
    }

    public static List<VehicleCategory> calculateCompatibleCategories(VehicleCategory maxCategory) {
        if (maxCategory == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(VehicleCategory.values())
                .filter(maxCategory::canAccommodate)
                .toList();
    }

    public static ParkingSpaceResponse fromEntity(ParkingSpace space) {
        if (space == null) {
            return null;
        }

        ParkingSpaceResponse response = new ParkingSpaceResponse();
        response.setId(space.getId());
        if (space.getOwner() != null) {
            response.setListedBy(space.getOwner().getId());
        }
        response.setDescription(space.getDescription());
        response.setHourlyPrice(space.getHourlyPrice());
        response.setAddress(space.getAddress());
        response.setLatitude(space.getLatitude());
        response.setLongitude(space.getLongitude());
        response.setMaximumVehicleCategory(space.getMaximumVehicleCategory());
        response.setCompatibleVehicleCategories(calculateCompatibleCategories(space.getMaximumVehicleCategory()));
        response.setPhoto1(space.getPhoto1());
        response.setPhoto2(space.getPhoto2());

        // Check if temporary pause has expired
        if (space.getStatus() == ParkingSpaceStatus.PAUSED
                && space.getPauseUntil() != null
                && !space.getPauseUntil().isAfter(LocalDateTime.now())) {
            response.setStatus(ParkingSpaceStatus.ACTIVE);
            response.setPauseUntil(null);
        } else {
            response.setStatus(space.getStatus());
            response.setPauseUntil(space.getPauseUntil());
        }

        response.setCreatedAt(space.getCreatedAt());
        response.setUpdatedAt(space.getUpdatedAt());
        return response;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getListedBy() {
        return listedBy;
    }

    public void setListedBy(Long listedBy) {
        this.listedBy = listedBy;
    }

    public Long getListedById() {
        return listedBy;
    }

    public void setListedById(Long listedById) {
        this.listedBy = listedById;
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

    public List<VehicleCategory> getCompatibleVehicleCategories() {
        return compatibleVehicleCategories;
    }

    public void setCompatibleVehicleCategories(List<VehicleCategory> compatibleVehicleCategories) {
        this.compatibleVehicleCategories = compatibleVehicleCategories;
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

    public ParkingSpaceStatus getStatus() {
        return status;
    }

    public void setStatus(ParkingSpaceStatus status) {
        this.status = status;
    }

    public LocalDateTime getPauseUntil() {
        return pauseUntil;
    }

    public void setPauseUntil(LocalDateTime pauseUntil) {
        this.pauseUntil = pauseUntil;
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
