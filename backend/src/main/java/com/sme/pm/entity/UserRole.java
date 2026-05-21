package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class UserRole {
    private Long userId;
    private Long roleId;
}
