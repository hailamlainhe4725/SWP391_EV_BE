package com.example.demo.entity;

import com.example.demo.enums.OwnershipStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ownership")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ownership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ownershipId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    Double totalSharePercentage = 0.0;

    @Enumerated(EnumType.STRING)
    OwnershipStatus status = OwnershipStatus.ACTIVE;

    LocalDateTime createdAt = LocalDateTime.now();
}
