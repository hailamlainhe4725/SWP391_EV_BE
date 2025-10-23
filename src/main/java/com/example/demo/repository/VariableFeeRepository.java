package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.response.FeeResponse;
import com.example.demo.entity.User;
import com.example.demo.entity.VariableFee;
import com.example.demo.entity.Vehicle;
@Repository
public interface VariableFeeRepository extends JpaRepository<VariableFee, Long> {
    List<VariableFee> findByVehicle(Vehicle vehicle);

    List<FeeResponse> findByDeletedFalse();

    List<VariableFee> findUnbilledByUserAndVehicle(User user, Vehicle vehicle);

    List<VariableFee> findByVehicleAndUserAndDeletedFalseAndCreatedAtBetween(
        Vehicle vehicle,
        User user,
        LocalDateTime startOfMonth,
        LocalDateTime endOfMonth
    );
}
