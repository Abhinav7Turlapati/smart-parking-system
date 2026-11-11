package com.smartparking.parking.dto;

import com.smartparking.parking.entity.VehicleBrand;

public class VehicleBrandResponse {

    private Long id;
    private String name;

    public VehicleBrandResponse() {
    }

    public VehicleBrandResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static VehicleBrandResponse fromEntity(VehicleBrand brand) {
        if (brand == null) {
            return null;
        }
        return new VehicleBrandResponse(brand.getId(), brand.getName());
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
}
