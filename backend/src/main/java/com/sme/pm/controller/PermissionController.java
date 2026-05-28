package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Permission;
import com.sme.pm.mapper.PermissionMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 * 提供系统权限查询API
 *
 * 用法：
 * - GET /api/permissions - 获取所有权限列表
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionMapper permissionMapper;

    public PermissionController(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * 获取所有权限列表
     * @return 权限列表
     */
    @GetMapping
    public Result<List<Permission>> getAll() {
        return Result.success(permissionMapper.findAll());
    }
}
