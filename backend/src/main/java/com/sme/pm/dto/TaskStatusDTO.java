package com.sme.pm.dto;

import lombok.Data;

@Data
public class TaskStatusDTO {
    private Long projectId;
    private String code;
    private String name;
    private String category;  // TODO, IN_PROGRESS, DONE
    private String color;
    private Integer sortOrder;
}
