package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.enums.DecisionType;
import com.example.demo.enums.VoteStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "vote_topic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoteTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long topicId;

    String title;
    String description;

    @Enumerated(EnumType.STRING)
    DecisionType decisionType; // MINOR, MEDIUM, MAJOR

    @ManyToOne
    @JoinColumn(name = "user_id")
    User createdBy;

    @ManyToOne
    @JoinColumn(name= "vehicle_id")
    Vehicle vehicle;

    Double requiredRatio; // 0.5 cho Medium, 0.75 cho Major...

    @Enumerated(EnumType.STRING)
    VoteStatus status; // PENDING, APPROVED, REJECTED, EXPIRED

    LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "deleted")
    boolean deleted = false;
}
