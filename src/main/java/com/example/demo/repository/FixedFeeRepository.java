package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.response.FeeResponse;
import com.example.demo.entity.FixedFee;
import com.example.demo.entity.Vehicle;
@Repository
public interface FixedFeeRepository extends JpaRepository<FixedFee, Long> {
    List<FixedFee> findByVehicle(Vehicle vehicle);

    List<FeeResponse> findByDeletedFalse();

        List<FixedFee> findByVehicleAndDeletedFalseAndCreatedAtBetween(
        Vehicle vehicle,
        LocalDateTime startOfMonth,
        LocalDateTime endOfMonth
    );
}