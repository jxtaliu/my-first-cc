package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;  // user:read, project:write, etc.

    private String name;

    private String module;  // user, project, task, timesheet, report

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
