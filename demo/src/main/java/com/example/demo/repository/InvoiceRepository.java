package com.example.demo.repository;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.User;

import lombok.Builder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUser(User user);


}