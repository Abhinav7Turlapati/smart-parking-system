package com.smartparking.parking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "vehicle_models",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_model_brand_name", columnNames = {"brand_id", "name"})
        }
)
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Model name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Brand is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private VehicleBrand brand;

    @NotNull(message = "Vehicle category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleCategory category;

    public VehicleModel() {
    }

    public VehicleModel(String name, VehicleBrand brand, VehicleCategory category) {
        this.name = name;
        this.brand = brand;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VehicleBrand getBrand() {
        return brand;
    }

    public void setBrand(VehicleBrand brand) {
        this.brand = brand;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
    }
}
