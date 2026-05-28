package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 提供用户管理相关API，包括用户CRUD、角色分配、部门管理等
 *
 * 用法：
 * - GET /api/users/me - 获取当前登录用户信息
 * - GET /api/users - 获取所有用户列表
 * - POST /api/users - 创建新用户
 * - PUT /api/users/{id} - 更新用户信息
 * - DELETE /api/users/{id} - 删除用户
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, UserService userService, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户ID（从Token中提取）
     * @return 用户信息（密码字段已清除）
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(@CurrentUser Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 获取所有用户列表
     * @return 所有用户信息列表
     */
    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userMapper.selectList(null));
    }

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }

    /**
     * 获取用户的角色列表
     * @param id 用户ID
     * @return 角色ID列表
     */
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        List<Long> roleIds = userMapper.findRolesByUserId(id).stream()
                .map(role -> role.getId())
                .toList();
        return Result.success(roleIds);
    }

    /**
     * 创建新用户
     * @param user 用户信息
     * @return 创建的用户信息
     */
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        return Result.success(userService.register(user));
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 更新后的用户信息
     * @return 成功返回空结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
        return Result.success();
    }

    /**
     * 为用户分配角色
     * @param id 用户ID
     * @param body 包含角色ID列表的请求体
     * @return 成功返回空结果
     */
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object roleIdsObj = body.get("roleIds");
        List<Long> roleIds = null;
        if (roleIdsObj instanceof List) {
            roleIds = ((List<?>) roleIdsObj).stream()
                    .map(o -> o instanceof Number ? ((Number) o).longValue() : Long.parseLong(o.toString()))
                    .toList();
        }
        userService.assignRoles(id, roleIds);
        return Result.success();
    }

    /**
     * 为用户分配部门
     * @param id 用户ID
     * @param body 包含部门ID的请求体
     * @return 成功返回空结果
     */
    @PutMapping("/{id}/department")
    public Result<Void> assignDepartment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object deptIdObj = body.get("departmentId");
        Long departmentId = deptIdObj != null ? (deptIdObj instanceof Number ? ((Number) deptIdObj).longValue() : Long.parseLong(deptIdObj.toString())) : null;
        userService.assignDepartment(id, departmentId);
        return Result.success();
    }

    /**
     * 移除用户的部门
     * @param id 用户ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}/department")
    public Result<Void> removeDepartment(@PathVariable Long id) {
        userService.assignDepartment(id, null);
        return Result.success();
    }

    /**
     * 删除用户（物理删除）
     * @param id 用户ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userMapper.deleteUserRolesByUserId(id);
        userMapper.physicalDeleteById(id);
        return Result.success();
    }
}
