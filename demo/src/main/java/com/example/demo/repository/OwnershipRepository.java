package com.example.demo.repository;

import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OwnershipRepository extends JpaRepository<Ownership, Long> {
    // find ownerships for a given user (not deleted)
    List<Ownership> findByUserAndDeletedFalse(User user);

    // all ownerships not deleted
    List<Ownership> findByDeletedFalse();

    // helper: find by id and not deleted
    Optional<Ownership> findByOwnershipIdAndDeletedFalse(Long ownershipId);

    Optional<Ownership> findByUser_UserIdAndVehicle_VehicleId(Long userId, Long vehicleId);

    List<Ownership> findByUser_Email(String email);
}
