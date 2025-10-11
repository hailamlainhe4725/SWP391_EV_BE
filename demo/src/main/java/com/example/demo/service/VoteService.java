package com.example.demo.service;

import com.example.demo.dto.request.CreateVoteRequest;
import com.example.demo.dto.request.CreateVoteTopicRequest;
import com.example.demo.dto.response.VoteResponse;
import com.example.demo.dto.response.VoteTopicResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteTopicRepository voteTopicRepository;
    private final OwnershipRepository ownershipRepository;
    private final UserRepository userRepository;

    // ====== Tạo chủ đề biểu quyết ======
    public VoteTopicResponse createTopic(CreateVoteTopicRequest req) {
        Ownership ownership = ownershipRepository.findById(req.getOwnershipId())
                .orElseThrow(() -> new RuntimeException("Ownership not found"));
        User creator = userRepository.findById(req.getCreatorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        double ratio = switch (req.getDecisionType()) {
            case MINOR -> 0.0;
            case MEDIUM -> 0.5;
            case MAJOR -> 0.75;
        };

        VoteTopic topic = VoteTopic.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .decisionType(req.getDecisionType())
                .ownership(ownership)
                .createdBy(creator)
                .requiredRatio(ratio)
                .status(VoteStatus.PENDING)
                .build();

        voteTopicRepository.save(topic);
        return mapTopicToResponse(topic);
    }

    // ====== Người dùng bỏ phiếu ======
    public VoteResponse castVote(CreateVoteRequest req) {
        VoteTopic topic = voteTopicRepository.findById(req.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ownership ownership = ownershipRepository.findByUser_UserIdAndVehicle_VehicleId(
                user.getId(), topic.getOwnership().getVehicle().getVehicleId())
                .orElseThrow(() -> new RuntimeException("Ownership not found for this vehicle"));

        voteRepository.findByTopicAndUser(topic, user).ifPresent(v -> {
            throw new RuntimeException("User already voted on this topic");
        });

        Vote vote = Vote.builder()
                .topic(topic)
                .user(user)
                .ownership(ownership)
                .choice(req.isAgree())
                .build();

        voteRepository.save(vote);
        return mapVoteToResponse(vote);
    }

    // ====== Tính kết quả ======
    public VoteTopicResponse calculateResult(Long topicId) {
        VoteTopic topic = voteTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (topic.getDecisionType() == DecisionType.MINOR) {
            topic.setStatus(VoteStatus.APPROVED);
            return mapTopicToResponse(voteTopicRepository.save(topic));
        }

        List<Vote> votes = voteRepository.findByTopic(topic);
        double totalWeight = votes.stream()
                .mapToDouble(v -> v.getOwnership().getTotalSharePercentage() / 100)
                .sum();

        double agreeWeight = votes.stream()
                .filter(Vote::getChoice)
                .mapToDouble(v -> v.getOwnership().getTotalSharePercentage() / 100)
                .sum();

        double ratio = totalWeight == 0 ? 0 : (agreeWeight / totalWeight);
        topic.setStatus(ratio >= topic.getRequiredRatio() ? VoteStatus.APPROVED : VoteStatus.REJECTED);

        voteTopicRepository.save(topic);
        return mapTopicToResponse(topic);
    }

    // ====== Lấy danh sách vote của 1 chủ đề ======
    public List<VoteResponse> getVotes(Long topicId) {
        VoteTopic topic = voteTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        return voteRepository.findByTopic(topic)
                .stream()
                .map(this::mapVoteToResponse)
                .collect(Collectors.toList());
    }

    // ====== MAPPING ======
    private VoteResponse mapVoteToResponse(Vote v) {
        return VoteResponse.builder()
                .voteId(v.getVoteId())
                .topicId(v.getTopic().getTopicId())
                .userId(v.getUser().getId())
                .userName(v.getUser().getFullName())
                .choice(v.getChoice())
                .weight(v.getOwnership().getTotalSharePercentage())
                .votedAt(v.getVotedAt())
                .build();
    }

    private VoteTopicResponse mapTopicToResponse(VoteTopic t) {
        return VoteTopicResponse.builder()
                .topicId(t.getTopicId())
                .title(t.getTitle())
                .description(t.getDescription())
                .decisionType(t.getDecisionType())
                .requiredRatio(t.getRequiredRatio())
                .status(t.getStatus())
                .ownershipId(t.getOwnership().getOwnershipId())
                .ownershipVehicleName(t.getOwnership().getVehicle().getBrand() + " " + t.getOwnership().getVehicle().getModel())
                .createdById(t.getCreatedBy().getId())
                .createdByName(t.getCreatedBy().getFullName())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
