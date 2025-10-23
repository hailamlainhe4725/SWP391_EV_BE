package com.example.demo.service;

import com.example.demo.dto.request.CreateVoteRequest;
import com.example.demo.dto.request.CreateVoteTopicRequest;
import com.example.demo.dto.response.VoteResponse;
import com.example.demo.dto.response.VoteTopicResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {
        private final VoteRepository voteRepository;
        private final VoteTopicRepository voteTopicRepository;
        private final OwnershipRepository ownershipRepository;
        private final UserRepository userRepository;
        private final VehicleRepository vehicleRepository;
        

        // ====== Tạo chủ đề biểu quyết ======
        public VoteTopicResponse createTopic(Authentication authentication,CreateVoteTopicRequest req) {
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Ownership not found"));
                User creator = userRepository.findByEmail(authentication.getName())
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
                                .vehicle(vehicle)
                                .createdBy(creator)
                                .requiredRatio(ratio)
                                .status(VoteStatus.PENDING)
                                .build();

                voteTopicRepository.save(topic);
                return mapTopicToResponse(topic);
        }

        public List<VoteTopicResponse> getAllVoteTopic(){
        return voteTopicRepository.findAll().stream()
                                .filter(b -> !b.isDeleted())
                                .map(this::mapTopicToResponse)
                                .collect(Collectors.toList());
        }

        public List<VoteTopicResponse> getUserTopic(Authentication authentication) {
    // Lấy thông tin user từ token
    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Lấy tất cả ownership mà user này tham gia
    List<Ownership> ownerships = ownershipRepository.findByUserAndDeletedFalse(user);
    if (ownerships.isEmpty()) {
        throw new ResourceNotFoundException("User does not own any vehicles or groups");
    }

    // Lấy tất cả vehicle tương ứng với ownerships
    List<Vehicle> vehicles = new ArrayList<>();
 for (Ownership o : ownerships) {
        if (o.getVehicle() != null) {
            vehicles.add(o.getVehicle());
        }
    }

    // Lấy tất cả voteTopic của các  vehicle này)
    List<VoteTopic> allTopics = voteTopicRepository.findByVehicleIn(vehicles);

    // Chuyển thành response (lọc ra những topic chưa bị xóa)
    return allTopics.stream()
            .filter(topic -> !topic.isDeleted())
            .map(this::mapTopicToResponse)
            .collect(Collectors.toList());
}

        // ====== Người dùng bỏ phiếu ======
        public VoteResponse castVote(Authentication authentication,CreateVoteRequest req) {
                VoteTopic topic = voteTopicRepository.findById(req.getTopicId())
                                .orElseThrow(() -> new RuntimeException("Topic not found"));
                User user = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Ownership ownership = ownershipRepository.findByUser_IdAndVehicle_VehicleId(
                                user.getId(), topic.getVehicle().getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Ownership not found for this vehicle"));

                voteRepository.findByTopicAndUser(topic, user).ifPresent(v -> {
                        throw new RuntimeException("User already voted on this topic");
                });

                Vote vote = Vote.builder()
                                .topic(topic)
                                .user(user)
                                .percentCoOwner(ownership.getTotalSharePercentage())
                                .vehicle(topic.getVehicle())
                                .choice(req.isAgree())
                                .build();

                voteRepository.save(vote);
                return mapVoteToResponse(vote);
        }

// ====== Tính kết quả ======
public VoteTopicResponse calculateResult(Long topicId) {
    VoteTopic topic = voteTopicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("Topic not found"));

    // Nếu là quyết định minor thì duyệt nhanh
    if (topic.getDecisionType() == DecisionType.MINOR) {
        topic.setStatus(VoteStatus.APPROVED);
        voteTopicRepository.save(topic);
        return mapTopicToResponse(topic);
    }

    // Lấy danh sách tất cả vote của topic
    List<Vote> votes = voteRepository.findByTopic(topic);
    if (votes.isEmpty()) {
        throw new RuntimeException("No votes found for this topic");
    }

    double totalWeight = 0.0;
    double agreeWeight = 0.0;
    Ownership ownership;
    for (Vote v : votes) {
        ownership = ownershipRepository.findByUser_IdAndVehicle_VehicleId(v.getUser().getId(), v.getVehicle().getVehicleId())
        .orElse(null);
        

        double weight = ownership.getTotalSharePercentage() / 100.0;
        totalWeight += weight;

        if (Boolean.TRUE.equals(v.getChoice())) {
            agreeWeight += weight;
        }
    }

    double ratio = totalWeight == 0 ? 0 : (agreeWeight / totalWeight);

    topic.setStatus(ratio >= topic.getRequiredRatio()
            ? VoteStatus.APPROVED
            : VoteStatus.REJECTED);

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
                                .weight(v.getPercentCoOwner())
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
                                .vehicleName(t.getVehicle().getModel()+t.getVehicle().getPlateNumber())
                                .createdById(t.getCreatedBy().getId())
                                .createdByName(t.getCreatedBy().getFullName())
                                .createdAt(t.getCreatedAt())
                                .build();
        }
}
