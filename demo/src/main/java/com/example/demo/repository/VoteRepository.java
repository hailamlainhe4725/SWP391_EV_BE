package com.example.demo.repository;

import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteTopic;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByTopic(VoteTopic topic);

    Optional<Vote> findByTopicAndUser(VoteTopic topic, User user);
}