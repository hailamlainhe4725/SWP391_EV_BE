package com.example.demo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.SumaInvoice;
import com.example.demo.entity.User;

public interface SumaInvoiceRepository extends JpaRepository<SumaInvoice, Long> {
    Optional<SumaInvoice> findByUserAndMonth(User user, String month);
}
