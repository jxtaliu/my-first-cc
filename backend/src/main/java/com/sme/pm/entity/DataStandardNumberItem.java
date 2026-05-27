package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("data_standard_number_item")
public class DataStandardNumberItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long standardId;  // 关联标准ID

    private BigDecimal minValue;  // 最小值

    private BigDecimal maxValue;  // 最大值

    private Integer decimalPlaces = 0;  // 小数位数

    private String example;  // 示例
}
