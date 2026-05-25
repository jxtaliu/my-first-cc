package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectId;  // Business key, e.g., PRJ_001

    private String name;

    private String description;

    private String projectType;  // DEVELOPE/CUSTOM

    private String status = "PLANNING";  // PLANNING/ACTIVE/COMPLETED/ARCHIVED

    private String sprintMode = "SCRUM";  // SCRUM/KANBAN

    private Long ownerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private Integer memberCount;

    @TableField(exist = false)
    private Integer taskCount;

    @TableField(exist = false)
    private Integer completedTaskCount;

    @TableField(exist = false)
    private Integer epicCount;

    @TableField(exist = false)
    private Integer featureCount;

    @TableField(exist = false)
    private Integer storyCount;

    @TableField(exist = false)
    private Integer bugCount;

    @TableField(exist = false)
    private Integer subtaskCount;
}
