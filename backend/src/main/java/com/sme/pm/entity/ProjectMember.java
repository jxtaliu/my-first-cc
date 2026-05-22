package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project_member")
public class ProjectMember {
    private String projectId; // References project.project_id
    private Long userId;
    private LocalDateTime joinedAt;
}
