package com.smartparking.parking.dto;

import com.smartparking.parking.entity.VehicleCategory;
import com.smartparking.parking.entity.VehicleModel;

public class VehicleModelResponse {

    private Long id;
    private String name;
    private Long brandId;
    private String brandName;
    private VehicleCategory category;

    public VehicleModelResponse() {
    }

    public VehicleModelResponse(Long id, String name, Long brandId, String brandName, VehicleCategory category) {
        this.id = id;
        this.name = name;
        this.brandId = brandId;
        this.brandName = brandName;
        this.category = category;
    }

    public static VehicleModelResponse fromEntity(VehicleModel model) {
        if (model == null) {
            return null;
        }
        Long bId = model.getBrand() != null ? model.getBrand().getId() : null;
        String bName = model.getBrand() != null ? model.getBrand().getName() : null;
        return new VehicleModelResponse(model.getId(), model.getName(), bId, bName, model.getCategory());
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

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
    }
}
