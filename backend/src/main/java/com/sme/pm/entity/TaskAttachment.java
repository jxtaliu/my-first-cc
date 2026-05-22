package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_attachment")
public class TaskAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String mimeType;

    private Long uploadedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
