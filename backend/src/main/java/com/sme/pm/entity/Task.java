package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long sprintId;

    private Long parentId;  // null for root tasks

    private Integer depth;  // 1-4, max 4 levels

    private String title;

    private String description;

    private Integer type;  // 1: epic, 2: feature, 3: story, 4: sub-task

    private Integer status;  // References task_status table

    private String priority;  // P0, P1, P2, P3

    private Long assigneeId;

    private Integer estimateHours;

    private Integer remainingHours;

    private Integer actualHours;

    private Integer progress;  // 0-100

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
}
