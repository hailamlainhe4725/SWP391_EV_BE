package com.example.demo.controller;

import com.example.demo.dto.request.CastVoteRequest;
import com.example.demo.dto.request.CreateVoteTopicRequest;
import com.example.demo.dto.response.VoteResponse;
import com.example.demo.dto.response.VoteTopicResponse;
import com.example.demo.enums.DecisionType;
import com.example.demo.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    // ===== STAFF tạo chủ đề biểu quyết =====
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/topics")
    public ResponseEntity<VoteTopicResponse> createTopic(@RequestBody CreateVoteTopicRequest req) {
        VoteTopicResponse res = voteService.createTopic(
                req.getOwnershipId(),
                req.getCreatorId(),
                req.getTitle(),
                req.getDescription(),
                req.getDecisionType());
        return ResponseEntity.ok(res);
    }

    // ===== USER bỏ phiếu =====
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<VoteResponse> castVote(@RequestBody CastVoteRequest req) {
        VoteResponse res = voteService.castVote(
                req.getTopicId(),
                req.getUserId(),
                req.isAgree(),
                req.getWeight());
        return ResponseEntity.ok(res);
    }

    // ===== STAFF tính kết quả của một topic =====
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/topics/{topicId}/calculate")
    public ResponseEntity<VoteTopicResponse> calculateResult(@PathVariable Long topicId) {
        VoteTopicResponse res = voteService.calculateResult(topicId);
        return ResponseEntity.ok(res);
    }

    // ===== USER hoặc STAFF xem danh sách phiếu của topic =====
    @PreAuthorize("hasAnyRole('USER','STAFF')")
    @GetMapping("/topics/{topicId}")
    public ResponseEntity<List<VoteResponse>> getVotes(@PathVariable Long topicId) {
        List<VoteResponse> res = voteService.getVotes(topicId);
        return ResponseEntity.ok(res);
    }
}
