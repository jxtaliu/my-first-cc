package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_code")
public class DictCode {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dictTypeId;

    private String code;

    private String name;

    private String nameEn;

    private String nameZh;

    private Integer sortOrder;

    private String extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
