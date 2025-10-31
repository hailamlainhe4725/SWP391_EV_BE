package com.example.demo.repository;

import com.example.demo.dto.response.OwnerContractResponse;
import com.example.demo.entity.OwnerContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface OwnerContractRepository extends JpaRepository<OwnerContract, Long> {
    List<OwnerContract> findByDeletedFalse();

    List<OwnerContract> findByContract_ContractId(Long contractId);

    List<OwnerContract> findByContract_Vehicle_VehicleId(Long vehicleId);
    List<OwnerContract> findByUser_Id(Long userId);

}
