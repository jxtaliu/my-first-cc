package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_status")
public class TaskStatus {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectId; // References project.project_id

    private String code;  // TODO, IN_PROGRESS, etc.

    private String nameEn;

    private String nameZh;

    private String color;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
