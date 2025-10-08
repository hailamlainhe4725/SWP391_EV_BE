package com.example.demo.repository;

import com.example.demo.entity.OwnerShip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OwnershipRepository extends JpaRepository<OwnerShip, Long> {
    List<OwnerShip> findByOwner_UserId(Long userId);

    List<OwnerShip> findByVehicle_VehicleId(Long vehicleId);
}
