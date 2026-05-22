package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("status_transition")
public class StatusTransition {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectId; // References project.project_id  // NULL means system default

    private Long fromStatusId;  // Source status

    private Long toStatusId;  // Target status

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
