package com.sme.pm.dto;

import lombok.Data;

@Data
public class TaskDependencyDTO {
    private Long taskId;
    private Long dependsOnTaskId;
    private String dependencyType;  // FS, SS, FF, SF
}
