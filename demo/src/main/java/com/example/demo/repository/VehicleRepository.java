package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStatus(String status);

    boolean existsByPlateNumber(String plateNumber);
    List<Vehicle> findTop4ByDeletedFalseOrderByVehicleIdAsc();

    Vehicle findByVehicleId(Long vehicleId);

    Optional<Vehicle> findByIdAndDeletedFalse(Long vehicleId);
}
