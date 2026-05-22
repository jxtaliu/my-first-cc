package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project_template")
public class ProjectTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer sprintDuration;  // Sprint duration in days, 0 means no sprint

    private Integer enablePriority;  // 0: disabled, 1: enabled

    private String taskTypes;  // Comma-separated: EPIC,FEATURE,STORY,TASK,BUG

    private String defaultStatusFlow;  // JSON format status flow

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
