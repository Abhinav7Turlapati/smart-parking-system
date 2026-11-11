package com.smartparking.parking.repository;

import com.smartparking.parking.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByUserId(Long userId);

    List<Vehicle> findByUserIdAndActiveTrue(Long userId);

    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);

    Optional<Vehicle> findByIdAndUserIdAndActiveTrue(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndRegistrationNumberIgnoreCase(Long userId, String registrationNumber);

    boolean existsByUserIdAndRegistrationNumberIgnoreCaseAndIdNot(Long userId, String registrationNumber, Long id);
}
