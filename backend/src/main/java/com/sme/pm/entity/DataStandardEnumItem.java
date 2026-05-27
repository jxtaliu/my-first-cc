package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("data_standard_enum_item")
public class DataStandardEnumItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long standardId;  // 关联标准ID

    private String value;  // 枚举值

    private String label;  // 显示标签

    private Integer sortOrder = 0;  // 排序

    private String description;  // 说明
}
