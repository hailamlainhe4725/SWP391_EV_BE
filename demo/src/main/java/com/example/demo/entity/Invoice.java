package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.enums.BillingStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long invoiceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    String invoiceMonth;
    Double totalAmount;

    LocalDateTime issuedDate;
    LocalDateTime dueDate;
    String note;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<InvoiceDetail> details = new ArrayList<>();

    @Column(name = "deleted")
    boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "suma_invoice_id")
@JsonIgnore
private SumaInvoice sumaInvoice;
}
