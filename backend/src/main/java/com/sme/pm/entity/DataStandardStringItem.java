package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("data_standard_string_item")
public class DataStandardStringItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long standardId;  // 关联标准ID

    private Integer minLength = 1;  // 最小长度

    private Integer maxLength = 255;  // 最大长度

    private String pattern;  // 正则表达式

    private String example;  // 示例
}
