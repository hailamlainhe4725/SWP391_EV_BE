package com.example.demo.repository;

import com.example.demo.entity.OwnerContract;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerContractRepository extends JpaRepository<OwnerContract, Long> {
    List<OwnerContract> findByContractId(Integer contractId);
}