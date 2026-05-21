package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_type")
public class DictType {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
