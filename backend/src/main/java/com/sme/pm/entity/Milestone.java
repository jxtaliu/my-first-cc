package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("milestone")
public class Milestone {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private LocalDate targetDate;

    private Integer isCrossProject;  // 0: project milestone, 1: cross-project

    private String projectId; // References project.project_id  // NULL for cross-project milestones

    private String status;  // ACTIVE, ACHIEVED, FAILED

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
