package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerShip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownershipId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private double sharePercentage;
    private double purchasePrice;
    private LocalDate purchaseDate;

}