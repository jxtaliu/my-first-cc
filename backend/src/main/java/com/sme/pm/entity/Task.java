package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskId;  // Business key, e.g., TSK001

    private String projectId;  // References project.project_id, for fast query

    private Long sprintId;

    private Long parentId;  // null for root tasks

    private Integer depth;  // 1-4, max 4 levels

    private String title;

    private String description;

    private String type;  // EPIC/FEATURE/STORY/TASK/BUG/SUBTASK - from sys_dict_code TASK_TYPE_PM

    private Integer status;  // References task_status table (for STORY/TASK)

    private Long bugStatusId;  // References bug_status table (for BUG type only)

    private String priority;  // P0, P1, P2, P3

    private Long assigneeId;

    private Integer estimateHours;

    private Integer remainingHours;

    private Integer actualHours;

    private Integer storyPoints;  // For STORY type only

    private Integer progress;  // 0-100

    private Long milestoneId;  // Auto-linked from sprint when task is added

    private LocalDate startDate;

    private LocalDate dueDate;

    private LocalDateTime inProgressSince;  // When task entered in_progress status

    private LocalDateTime completionDate;

    private Integer version;  // Optimistic lock

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    @JsonProperty("children")
    private List<Task> children;
}
