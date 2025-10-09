package com.example.demo.repository;

import com.example.demo.entity.OwnerContract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OwnerContractRepository extends JpaRepository<OwnerContract, Long> {
    List<OwnerContract> findByDeletedFalse();
}
