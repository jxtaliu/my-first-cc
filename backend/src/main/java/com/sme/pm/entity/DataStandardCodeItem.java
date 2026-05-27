package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("data_standard_code_item")
public class DataStandardCodeItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long standardId;  // 关联标准ID

    private String format;  // 格式，如 [A-Z0000]

    private String prefix;  // 前缀

    private Integer length;  // 长度

    private String example;  // 示例
}
