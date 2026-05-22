package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sprint")
public class Sprint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer status;  // 1: planning, 2: active, 3: completed

    private Long milestoneId;  // Optional link to milestone

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
