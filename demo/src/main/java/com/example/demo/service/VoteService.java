package com.example.demo.service;

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
        public VoteTopicResponse createTopic(Long ownershipId, Long creatorId, String title, String description,
                        DecisionType type) {
                Ownership ownership = ownershipRepository.findById(ownershipId)
                                .orElseThrow(() -> new RuntimeException("Ownership not found"));
                User creator = userRepository.findById(creatorId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                double ratio = switch (type) {
                        case MINOR -> 0.0;
                        case MEDIUM -> 0.5;
                        case MAJOR -> 0.75;
                };

                VoteTopic topic = VoteTopic.builder()
                                .title(title)
                                .description(description)
                                .decisionType(type)
                                .ownership(ownership)
                                .createdBy(creator)
                                .requiredRatio(ratio)
                                .status(VoteStatus.PENDING)
                                .build();

                return mapTopicToResponse(voteTopicRepository.save(topic));
        }

        // ====== Người dùng bỏ phiếu ======
        public VoteResponse castVote(Long topicId, Long userId, boolean agree, double weight) {
                VoteTopic topic = voteTopicRepository.findById(topicId)
                                .orElseThrow(() -> new RuntimeException("Topic not found"));
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                voteRepository.findByTopicAndUser(topic, user)
                                .ifPresent(v -> {
                                        throw new RuntimeException("User already voted this topic");
                                });

                Vote vote = Vote.builder()
                                .topic(topic)
                                .user(user)
                                .ownership(topic.getOwnership())
                                .choice(agree)
                                .weight(weight)
                                .build();

                return mapVoteToResponse(voteRepository.save(vote));
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
                double totalWeight = votes.stream().mapToDouble(Vote::getWeight).sum();
                double agreeWeight = votes.stream()
                                .filter(Vote::getChoice)
                                .mapToDouble(Vote::getWeight)
                                .sum();

                double ratio = totalWeight == 0 ? 0 : (agreeWeight / totalWeight);
                topic.setStatus(ratio >= topic.getRequiredRatio() ? VoteStatus.APPROVED : VoteStatus.REJECTED);
                return mapTopicToResponse(voteTopicRepository.save(topic));
        }

        // ====== Lấy danh sách vote của 1 chủ đề ======
        public List<VoteResponse> getVotes(Long topicId) {
                VoteTopic topic = voteTopicRepository.findById(topicId)
                                .orElseThrow(() -> new RuntimeException("Topic not found"));
                return voteRepository.findByTopic(topic)
                                .stream().map(this::mapVoteToResponse)
                                .collect(Collectors.toList());
        }

        // ====== MAPPING ======
        private VoteTopicResponse mapTopicToResponse(VoteTopic t) {
                return VoteTopicResponse.builder()
                                .topicId(t.getTopicId())
                                .title(t.getTitle())
                                .description(t.getDescription())
                                .decisionType(t.getDecisionType())
                                .requiredRatio(t.getRequiredRatio())
                                .status(t.getStatus())
                                .createdBy(t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : null)
                                .ownershipId(t.getOwnership() != null ? t.getOwnership().getOwnershipId() : null)
                                .createdAt(t.getCreatedAt())
                                .build();
        }

        private VoteResponse mapVoteToResponse(Vote v) {
                return VoteResponse.builder()
                                .voteId(v.getVoteId())
                                .topicId(v.getTopic().getTopicId())
                                .topicTitle(v.getTopic().getTitle())
                                .userName(v.getUser().getFullName())
                                .choice(v.getChoice())
                                .weight(v.getWeight())
                                .votedAt(v.getVotedAt())
                                .build();
        }
}
