package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Permission;
import com.sme.pm.entity.Role;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.RoleMapper;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    public RoleController(RoleService roleService, RoleMapper roleMapper, UserMapper userMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<List<Role>> getAll() {
        return Result.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return role == null ? Result.success(null) : Result.success(role);
    }

    @PostMapping
    public Result<Role> create(@RequestBody Role role) {
        return Result.success(roleService.create(role));
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return Result.success(roleService.update(role));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!roleService.canDelete(id)) {
            return Result.error("Cannot delete: role has assigned users");
        }
        roleMapper.deleteRolePermissions(id);
        roleMapper.physicalDeleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Permission>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getPermissions(id));
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    @GetMapping("/{id}/users")
    public Result<List<User>> getUsers(@PathVariable Long id) {
        return Result.success(userMapper.findUsersByRoleId(id));
    }
}
