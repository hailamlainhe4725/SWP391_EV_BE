package com.example.demo.dto.request;

import lombok.Data;

@Data

public class CreateVoteRequest {
    private Long topicId;
    private boolean agree; // true = đồng ý, false = không
}
