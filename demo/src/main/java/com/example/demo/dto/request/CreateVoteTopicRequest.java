package com.example.demo.dto.request;

import com.example.demo.enums.DecisionType;
import lombok.Data;

@Data
public class CreateVoteTopicRequest {
    private Long ownershipId;
    private Long creatorId; // Staff ID
    private String title;
    private String description;
    private DecisionType decisionType; // MINOR, MEDIUM, MAJOR
}
