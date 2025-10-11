package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CastVoteRequest {
    private Long topicId;
    private Long userId;
    private boolean agree; // true = đồng ý, false = không
    private double weight; // tỷ lệ sở hữu user (ví dụ 0.25)
}
