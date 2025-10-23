package com.example.demo.repository;

import com.example.demo.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByUser_EmailAndDeletedFalse(String email);

    List<Contract> findByDeletedFalse();
}
