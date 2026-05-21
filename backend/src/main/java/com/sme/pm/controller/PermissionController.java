package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Permission;
import com.sme.pm.mapper.PermissionMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionMapper permissionMapper;

    public PermissionController(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @GetMapping
    public Result<List<Permission>> getAll() {
        return Result.success(permissionMapper.findAll());
    }
}
