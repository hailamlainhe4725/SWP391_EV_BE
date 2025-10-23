package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class VoteResponse {
    Long voteId;
    Long topicId;
    Long userId;
    String userName;
    Boolean choice;
    Double weight; // từ Ownership
    LocalDateTime votedAt;
}
