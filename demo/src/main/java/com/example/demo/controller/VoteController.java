package com.example.demo.controller;

import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteTopic;
import com.example.demo.enums.DecisionType;
import com.example.demo.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/topic")
    public ResponseEntity<VoteTopic> createTopic(
            @RequestParam Long ownershipId,
            @RequestParam Long creatorId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam DecisionType decisionType) {
        return ResponseEntity.ok(voteService.createTopic(ownershipId, creatorId, title, description, decisionType));
    }

    @PostMapping("/cast")
    public ResponseEntity<Vote> castVote(
            @RequestParam Long topicId,
            @RequestParam Long userId,
            @RequestParam boolean agree,
            @RequestParam double weight) {
        return ResponseEntity.ok(voteService.castVote(topicId, userId, agree, weight));
    }

    @GetMapping("/topic/{topicId}/result")
    public ResponseEntity<VoteTopic> calculateResult(@PathVariable Long topicId) {
        return ResponseEntity.ok(voteService.calculateResult(topicId));
    }

    @GetMapping("/topic/{topicId}/votes")
    public ResponseEntity<List<Vote>> getVotes(@PathVariable Long topicId) {
        return ResponseEntity.ok(voteService.getVotes(topicId));
    }
}
