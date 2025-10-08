package com.example.demo.repository;

import com.example.demo.entity.Vehicle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStatus(String status);

    boolean existsByLicensePlate(String licensePlate);
}
