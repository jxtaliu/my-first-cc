package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer projectType = 1;  // 1: internal, 2: external/client

    private Integer status = 1;  // 1: planning, 2: active, 3: archived

    private Integer sprintMode = 1;  // 1: fixed, 2: agile, 3: kanban

    private Long ownerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
