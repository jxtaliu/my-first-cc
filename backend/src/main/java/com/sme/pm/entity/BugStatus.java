package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bug_status")
public class BugStatus {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectId; // NULL means system default

    private String code; // OPEN, IN_PROGRESS, IN_TEST, CLOSED, REOPENED

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
