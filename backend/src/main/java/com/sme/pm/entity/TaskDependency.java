package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_dependency")
public class TaskDependency {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;  // Current task

    private Long dependsOnTaskId;  // Dependency task

    private String dependencyType;  // FS: Finish-Start, SS: Start-Start, FF: Finish-Finish, SF: Start-Finish

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
