package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.VariableFee;

public interface FeeRepository extends JpaRepository<VariableFee, Integer> {
    // Kết hợp cả FixedFee và VariableFee trong 1 service, nên chỉ cần 1 repo
}
