package com.sme.pm.dto;

import lombok.Data;

@Data
public class TaskStatusDTO {
    private Long projectId;
    private String code;
    private String name;
    private String color;
    private Integer sortOrder;
}
