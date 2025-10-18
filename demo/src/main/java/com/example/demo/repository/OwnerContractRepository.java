package com.example.demo.repository;

import com.example.demo.dto.response.OwnerContractResponse;
import com.example.demo.entity.OwnerContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OwnerContractRepository extends JpaRepository<OwnerContract, Long> {
    List<OwnerContract> findByDeletedFalse();

    List<OwnerContract> findByContract_ContractId(Long contractId);

    List<OwnerContract> findByContract_Vehicle_VehicleId(Long vehicleId);

}
