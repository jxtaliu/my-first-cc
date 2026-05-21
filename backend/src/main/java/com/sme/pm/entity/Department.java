package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String departmentId;  // Business ID, e.g. DEPT001

    private String name;

    private Long parentId;  // null for root departments

    private Long leaderId;

    private Integer sortOrder;

    private Integer status;  // 1: enabled, 0: disabled

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<Department> children;
}
