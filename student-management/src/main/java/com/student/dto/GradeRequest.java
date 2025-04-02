package com.student.dto;

import lombok.Data;

@Data
public class GradeRequest {
    private Long submissionId;
    private Integer score;
    private String comment;
}
