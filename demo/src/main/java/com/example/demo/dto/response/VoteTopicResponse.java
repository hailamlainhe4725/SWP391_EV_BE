package com.example.demo.dto.response;

import com.example.demo.enums.DecisionType;
import com.example.demo.enums.VoteStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class VoteTopicResponse {
    Long topicId;
    String title;
    String description;
    DecisionType decisionType;
    Double requiredRatio;
    VoteStatus status;



    String vehicleName;
    Long createdById;
    String createdByName;

    LocalDateTime createdAt;
}
