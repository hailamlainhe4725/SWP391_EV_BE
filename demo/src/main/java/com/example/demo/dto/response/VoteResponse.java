package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteResponse {
    private Long voteId;
    private Long relatedId;
    private Long userId;
    private Boolean agree;
    private LocalDateTime createdAt;
}
