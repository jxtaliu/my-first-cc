package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bug_status_transition")
public class BugStatusTransition {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectId; // NULL means system default

    private String fromStatus; // Source status code

    private String toStatus; // Target status code

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
