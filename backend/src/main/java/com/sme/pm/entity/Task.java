package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sprintId;

    private Long parentId;  // null for root tasks

    private Integer depth;  // 1-4, max 4 levels

    private String title;

    private String description;

    private Integer type;  // 1: epic, 2: feature, 3: story, 4: sub-task

    private Integer status;  // 1: todo, 2: in_progress, 3: done

    private Long assigneeId;

    private Integer estimateHours;

    private Integer actualHours;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
