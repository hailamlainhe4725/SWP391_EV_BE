package com.example.demo.repository;

import com.example.demo.entity.Ownership;
import com.example.demo.entity.VoteTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VoteTopicRepository extends JpaRepository<VoteTopic, Long> {
    List<VoteTopic> findByOwnership(Ownership ownership);
    List<VoteTopic> findByOwnershipIn(List<Ownership> ownerships);
}