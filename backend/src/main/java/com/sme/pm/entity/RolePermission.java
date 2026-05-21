package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_role_permission")
public class RolePermission {
    private Long roleId;
    private Long permissionId;
}
