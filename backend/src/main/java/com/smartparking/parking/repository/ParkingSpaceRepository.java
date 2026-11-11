package com.smartparking.parking.repository;

import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.entity.ParkingSpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {

    List<ParkingSpace> findByOwnerId(Long ownerId);

    List<ParkingSpace> findByOwnerIdAndStatus(Long ownerId, ParkingSpaceStatus status);

    List<ParkingSpace> findByOwnerIdAndStatusNot(Long ownerId, ParkingSpaceStatus status);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    Optional<ParkingSpace> findByIdAndOwnerId(Long id, Long ownerId);
}
