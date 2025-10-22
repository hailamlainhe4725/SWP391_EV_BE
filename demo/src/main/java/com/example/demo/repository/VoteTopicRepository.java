package com.example.demo.repository;

import com.example.demo.entity.Ownership;
import com.example.demo.entity.VoteTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface VoteTopicRepository extends JpaRepository<VoteTopic, Long> {
    List<VoteTopic> findByOwnership(Ownership ownership);
    List<VoteTopic> findByOwnershipIn(List<Ownership> ownerships);
}