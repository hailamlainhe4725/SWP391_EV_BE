// dto/response/VoteResponse.java
package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class VoteResponse {
    Long voteId;
    Long topicId;
    String topicTitle;
    String userName;
    Boolean choice;
    Double weight;
    LocalDateTime votedAt;
}
