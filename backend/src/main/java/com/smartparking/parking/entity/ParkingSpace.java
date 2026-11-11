package com.smartparking.parking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_spaces")
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Owner is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Hourly price is required")
    @Positive(message = "Hourly price must be greater than 0")
    @Column(name = "hourly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyPrice;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    @NotNull(message = "Latitude is required")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(nullable = false)
    private Double longitude;

    @NotNull(message = "Maximum vehicle category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "maximum_vehicle_category", nullable = false)
    private VehicleCategory maximumVehicleCategory;

    @NotBlank(message = "photo1 is required")
    @Column(nullable = false)
    private String photo1;

    @Column
    private String photo2;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSpaceStatus status = ParkingSpaceStatus.ACTIVE;

    @Column(name = "pause_until")
    private LocalDateTime pauseUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ParkingSpace() {
    }

    public ParkingSpace(User owner, String description, BigDecimal hourlyPrice, String address,
                        Double latitude, Double longitude, VehicleCategory maximumVehicleCategory,
                        String photo1, String photo2, ParkingSpaceStatus status) {
        this.owner = owner;
        this.description = description;
        this.hourlyPrice = hourlyPrice;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maximumVehicleCategory = maximumVehicleCategory;
        this.photo1 = photo1;
        this.photo2 = photo2;
        this.status = status != null ? status : ParkingSpaceStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = ParkingSpaceStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    // Alias for owner relationship
    public User getListedBy() {
        return owner;
    }

    public void setListedBy(User listedBy) {
        this.owner = listedBy;
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
