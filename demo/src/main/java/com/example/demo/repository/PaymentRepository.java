package com.example.demo.repository;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.Payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoice(Invoice invoice);

List<Payment> findByInvoice_User_Id(Long Id);

}