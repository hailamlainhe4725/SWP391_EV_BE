package com.example.demo.repository;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;

import lombok.Builder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUser(User user);

    boolean existsByUserAndVehicleAndIssuedDateBetween(User user, Vehicle vehicle, LocalDateTime startOfMonth,
            LocalDateTime endOfMonth);


}