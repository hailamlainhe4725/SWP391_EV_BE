package com.example.demo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.dto.response.FeeResponse;
import com.example.demo.entity.FixedFee;
import com.example.demo.entity.Vehicle;

public interface FixedFeeRepository extends JpaRepository<FixedFee, Long> {
    List<FixedFee> findByVehicle(Vehicle vehicle);

    List<FeeResponse> findByDeletedFalse();
}