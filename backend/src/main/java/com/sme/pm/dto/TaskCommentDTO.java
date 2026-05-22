package com.sme.pm.dto;

import lombok.Data;

@Data
public class TaskCommentDTO {
    private Long taskId;
    private Long parentCommentId;
    private String content;
}
