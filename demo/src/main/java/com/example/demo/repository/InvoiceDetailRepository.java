package com.example.demo.repository;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceDetail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {

    List<InvoiceDetail> findByInvoiceId(Integer invoiceId);
}