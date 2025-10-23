package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import com.example.demo.enums.BookingStatus;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bookingId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    LocalDateTime startTime;

    @Column(nullable = false)
    LocalDateTime endTime;

    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "priority_score")
    Double priorityScore;
    @Column(nullable = false)
    @Builder.Default

    @Enumerated(EnumType.STRING)
    BookingStatus bookingStatus = BookingStatus.Pending; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    @Column(name = "deleted")
    boolean deleted = false;
}
