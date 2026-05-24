package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sprint")
public class Sprint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectId; // References project.project_id

    private String name;

    private String goal; // Sprint goal

    private LocalDate startDate;

    private LocalDate endDate;

    private String status; // PLANNING, ACTIVE, COMPLETED, ARCHIVED

    private Integer capacityHours; // Team capacity in hours

    private Integer velocity; // Story points completed

    private Long milestoneId; // Optional link to milestone

    private Integer startReminderSent;

    private Integer endReminderSent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
