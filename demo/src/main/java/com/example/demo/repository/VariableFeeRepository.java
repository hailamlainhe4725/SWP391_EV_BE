package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.VariableFee;
import com.example.demo.entity.Vehicle;

public interface VariableFeeRepository extends JpaRepository<VariableFee, Long> {
    List<VariableFee> findByVehicle(Vehicle vehicle);
}
