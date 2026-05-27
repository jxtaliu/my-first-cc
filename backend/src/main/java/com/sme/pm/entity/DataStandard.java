package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("data_standard")
public class DataStandard {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;  // 标准编码

    private String name;  // 标准名称

    private String type;  // ENUM/CODE/STRING/NUMBER

    private String description;  // 描述

    private Long ownerId;  // 责任人ID

    private String ownerName;  // 责任人姓名

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<DataStandardEnumItem> enumItems;

    @TableField(exist = false)
    private DataStandardCodeItem codeItem;

    @TableField(exist = false)
    private DataStandardStringItem stringItem;

    @TableField(exist = false)
    private DataStandardNumberItem numberItem;
}
