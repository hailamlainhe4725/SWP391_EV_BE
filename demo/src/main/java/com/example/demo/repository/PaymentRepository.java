package com.example.demo.repository;

import com.example.demo.entity.Payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByInvoiceId(Integer invoiceId);
}
