package com.example.demo.service;

import com.example.demo.dto.request.CreateVoteRequest;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;
    @Mock
    private VoteTopicRepository voteTopicRepository;
    @Mock
    private OwnershipRepository ownershipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private VoteService voteService;

    private Vehicle vehicle;
    private VoteTopic topic;
    private User staff, co1, co2, co3, co4;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        staff = User.builder().id(2L).email("staff@example.com").fullName("Staff").build();
        co1 = User.builder().id(1L).email("u1@example.com").fullName("User1").build();
        co2 = User.builder().id(5L).email("u5@example.com").fullName("User5").build();
        co3 = User.builder().id(7L).email("u7@example.com").fullName("User7").build();
        co4 = User.builder().id(8L).email("u8@example.com").fullName("User8").build();

        vehicle = Vehicle.builder().vehicleId(3L).model("ModelX").plateNumber("ABC-123").build();

        topic = VoteTopic.builder()
                .topicId(2L)
                .title("Thay động cơ chính")
                .description("Major test")
                .decisionType(DecisionType.MAJOR)
                .requiredRatio(0.75)
                .status(VoteStatus.PENDING)
                .vehicle(vehicle)
                .createdBy(staff)
                .build();
    }

    // ==========================
    // 1️⃣ Test castVote()
    // ==========================
    @Test
    void testCastVote_Success() {
        // Giả lập user đăng nhập
        when(authentication.getName()).thenReturn("u1@example.com");
        when(userRepository.findByEmail("u1@example.com")).thenReturn(Optional.of(co1));
        when(voteTopicRepository.findById(2L)).thenReturn(Optional.of(topic));

        Ownership ownership = Ownership.builder()
                .user(co1)
                .vehicle(vehicle)
                .totalSharePercentage(10.0)
                .build();

        when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(1L, 3L))
                .thenReturn(Optional.of(ownership));
        when(voteRepository.findByTopicAndUser(topic, co1)).thenReturn(Optional.empty());

        CreateVoteRequest req = new CreateVoteRequest();
        req.setTopicId(2L);
        req.setAgree(true);

        Vote savedVote = Vote.builder()
                .voteId(10L)
                .topic(topic)
                .user(co1)
                .vehicle(vehicle)
                .percentCoOwner(10.0)
                .choice(true)
                .build();

        when(voteRepository.save(any(Vote.class))).thenReturn(savedVote);

        var result = voteService.castVote(authentication, req);

        assertNotNull(result);
        assertEquals(true, result.getChoice());
        assertEquals(10.0, result.getWeight());
        assertEquals(co1.getFullName(), result.getUserName());
    }

    // ==========================
    // 2️⃣ Test calculateResult()
    // ==========================
    @Test
    void testCalculateResult_Approved() {
        when(voteTopicRepository.findById(2L)).thenReturn(Optional.of(topic));

        // Dữ liệu vote: tổng 80% đồng ý
        List<Vote> votes = List.of(
                Vote.builder().user(co1).vehicle(vehicle).choice(true).build(),
                Vote.builder().user(co2).vehicle(vehicle).choice(true).build(),
                Vote.builder().user(co3).vehicle(vehicle).choice(false).build(),
                Vote.builder().user(co4).vehicle(vehicle).choice(true).build()
        );

        when(voteRepository.findByTopic(topic)).thenReturn(votes);

        when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(1L, 3L))
                .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(10.0).build()));
        when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(5L, 3L))
                .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(50.0).build()));
        when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(7L, 3L))
                .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(20.0).build()));
        when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(8L, 3L))
                .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(20.0).build()));

        voteService.calculateResult(2L);

        assertEquals(VoteStatus.APPROVED, topic.getStatus());
    }

    @Test
void testCalculateResult_Rejected_60Percent() {
    when(voteTopicRepository.findById(2L)).thenReturn(Optional.of(topic));

    // Dữ liệu vote: 60% đồng ý, 40% từ chối
    List<Vote> votes = List.of(
            Vote.builder().user(co1).vehicle(vehicle).choice(true).build(),   // 10%
            Vote.builder().user(co2).vehicle(vehicle).choice(true).build(),   // 50%
            Vote.builder().user(co3).vehicle(vehicle).choice(false).build(),  // 20%
            Vote.builder().user(co4).vehicle(vehicle).choice(false).build()   // 20%
    );

    when(voteRepository.findByTopic(topic)).thenReturn(votes);

    when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(1L, 3L))
            .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(10.0).build()));
    when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(5L, 3L))
            .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(50.0).build()));
    when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(7L, 3L))
            .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(20.0).build()));
    when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(8L, 3L))
            .thenReturn(Optional.of(Ownership.builder().totalSharePercentage(20.0).build()));

    voteService.calculateResult(2L);

    assertEquals(VoteStatus.REJECTED, topic.getStatus());
}



    @Test
    void testCastVote_TopicNotFound() {
    when(voteTopicRepository.findById(99L)).thenReturn(Optional.empty());
    CreateVoteRequest req = new CreateVoteRequest();
    req.setTopicId(99L);
    req.setAgree(true);

    assertThrows(RuntimeException.class, () -> voteService.castVote(authentication, req));
}


@Test
void testCastVote_UserAlreadyVoted() {
    when(authentication.getName()).thenReturn("u1@example.com");
    when(userRepository.findByEmail("u1@example.com")).thenReturn(Optional.of(co1));
    when(voteTopicRepository.findById(2L)).thenReturn(Optional.of(topic));

    when(ownershipRepository.findByUser_IdAndVehicle_VehicleId(1L, 3L))
        .thenReturn(Optional.of(Ownership.builder().user(co1).vehicle(vehicle).totalSharePercentage(10.0).build()));

    when(voteRepository.findByTopicAndUser(topic, co1))
        .thenReturn(Optional.of(Vote.builder().topic(topic).user(co1).build()));

    CreateVoteRequest req = new CreateVoteRequest();
    req.setTopicId(2L);
    req.setAgree(true);

    assertThrows(RuntimeException.class, () -> voteService.castVote(authentication, req));
}




}
