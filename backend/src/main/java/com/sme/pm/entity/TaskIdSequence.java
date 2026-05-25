package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("task_id_sequence")
public class TaskIdSequence {
    private String projectId;

    private String type;

    private Integer currentSeq = 0;
}

