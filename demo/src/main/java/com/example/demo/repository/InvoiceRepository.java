package com.example.demo.repository;

import com.example.demo.entity.Invoice;

import lombok.Builder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByUserUserId(Long userId);

    List<Invoice> findByVehicleVehicleId(Integer vehicleId);

}