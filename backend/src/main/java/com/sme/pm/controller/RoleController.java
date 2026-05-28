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

/**
 * 角色控制器
 * 提供系统角色管理相关API，包括角色CRUD、权限分配、用户关联等
 *
 * 用法：
 * - GET /api/roles - 获取所有角色列表
 * - POST /api/roles - 创建角色
 * - PUT /api/roles/{id} - 更新角色
 * - DELETE /api/roles/{id} - 删除角色
 * - GET /api/roles/{id}/permissions - 获取角色权限列表
 * - PUT /api/roles/{id}/permissions - 分配角色权限
 * - GET /api/roles/{id}/users - 获取拥有该角色的用户列表
 */
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

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    @GetMapping
    public Result<List<Role>> getAll() {
        return Result.success(roleService.getAllRoles());
    }

    /**
     * 根据ID获取角色信息
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return role == null ? Result.success(null) : Result.success(role);
    }

    /**
     * 创建新角色
     * @param role 角色信息
     * @return 创建的角色信息
     */
    @PostMapping
    public Result<Role> create(@RequestBody Role role) {
        return Result.success(roleService.create(role));
    }

    /**
     * 更新角色信息
     * @param id 角色ID
     * @param role 更新后的角色信息
     * @return 更新后的角色信息
     */
    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return Result.success(roleService.update(role));
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 成功返回空结果；如果角色有分配用户则返回错误
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!roleService.canDelete(id)) {
            return Result.error("Cannot delete: role has assigned users");
        }
        roleMapper.deleteRolePermissions(id);
        roleMapper.physicalDeleteById(id);
        return Result.success();
    }

    /**
     * 获取角色的权限列表
     * @param id 角色ID
     * @return 权限列表
     */
    @GetMapping("/{id}/permissions")
    public Result<List<Permission>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getPermissions(id));
    }

    /**
     * 为角色分配权限
     * @param id 角色ID
     * @param body 包含permissionIds列表的请求体
     * @return 成功返回空结果
     */
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    /**
     * 获取拥有该角色的用户列表
     * @param id 角色ID
     * @return 用户列表
     */
    @GetMapping("/{id}/users")
    public Result<List<User>> getUsers(@PathVariable Long id) {
        return Result.success(userMapper.findUsersByRoleId(id));
    }
}
