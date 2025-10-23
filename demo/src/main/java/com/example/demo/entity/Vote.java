package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "vote")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long voteId;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    VoteTopic topic;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "ownership_id", nullable = false)
    Ownership ownership;

    Boolean choice; // true = đồng ý, false = không

    LocalDateTime votedAt = LocalDateTime.now();
    @Column(name = "deleted")
    boolean deleted = false;
}