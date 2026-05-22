package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;  // Recipient

    private String type;  // TASK_ASSIGNED, TASK_STATUS_CHANGED, MILESTONE_DUE, etc.

    private String title;

    private String content;

    private Long relatedTaskId;

    private Long relatedProjectId;

    private Integer isRead;  // 0: unread, 1: read

    private LocalDateTime readAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
