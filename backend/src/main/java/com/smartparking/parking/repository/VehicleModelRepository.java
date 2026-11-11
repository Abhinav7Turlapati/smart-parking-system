package com.smartparking.parking.repository;

import com.smartparking.parking.entity.VehicleCategory;
import com.smartparking.parking.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    List<VehicleModel> findByBrandIdOrderByNameAsc(Long brandId);

    List<VehicleModel> findByBrandNameIgnoreCaseOrderByNameAsc(String brandName);

    List<VehicleModel> findByCategory(VehicleCategory category);

    List<VehicleModel> findAllByOrderByNameAsc();

    Optional<VehicleModel> findByBrandIdAndNameIgnoreCase(Long brandId, String name);

    boolean existsByBrandIdAndNameIgnoreCase(Long brandId, String name);
}
