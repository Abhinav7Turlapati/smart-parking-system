package com.smartparking.parking.repository;

import com.smartparking.parking.entity.VehicleBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Long> {

    Optional<VehicleBrand> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<VehicleBrand> findAllByOrderByNameAsc();
}
