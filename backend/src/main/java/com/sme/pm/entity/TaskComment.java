package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_comment")
public class TaskComment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long userId;

    private String content;

    private Long parentCommentId;  // For replies

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
