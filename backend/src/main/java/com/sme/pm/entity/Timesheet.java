package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("timesheet")
public class Timesheet {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String projectId; // References project.project_id

    private Long taskId;  // nullable

    private LocalDateTime workDate;

    private Integer hours;

    private String description;

    private Integer approvalStatus;  // 1: pending, 2: approved, 3: rejected

    private Long approverId;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
