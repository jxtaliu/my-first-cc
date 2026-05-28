package com.sme.pm.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Department;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.DepartmentMapper;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 * 提供部门管理相关API，包括部门CRUD、部门树、用户部门分配等
 *
 * 用法：
 * - GET /api/departments - 获取所有部门列表
 * - GET /api/departments/tree - 获取部门树结构
 * - POST /api/departments - 创建部门
 * - PUT /api/departments/{id} - 更新部门
 * - DELETE /api/departments/{id} - 删除部门
 * - GET /api/departments/{id}/users - 获取部门下的用户列表
 * - POST /api/departments/{id}/users/{userId} - 添加用户到部门
 * - DELETE /api/departments/{id}/users/{userId} - 从部门移除用户
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    public DepartmentController(DepartmentService departmentService, DepartmentMapper departmentMapper, UserMapper userMapper) {
        this.departmentService = departmentService;
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
    }

    /**
     * 获取所有部门列表
     * @return 部门列表
     */
    @GetMapping
    public Result<List<Department>> getAll() {
        return Result.success(departmentService.getAllDepartments());
    }

    /**
     * 获取部门树结构
     * @return 部门树（包含层级关系）
     */
    @GetMapping("/tree")
    public Result<List<Department>> getTree() {
        return Result.success(departmentService.getDepartmentTree());
    }

    /**
     * 根据ID获取部门信息
     * @param id 部门ID
     * @return 部门信息
     */
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        Department dept = departmentService.getById(id);
        return dept == null ? Result.success(null) : Result.success(dept);
    }

    /**
     * 创建新部门
     * @param department 部门信息
     * @return 创建的部门信息
     */
    @PostMapping
    public Result<Department> create(@RequestBody Department department) {
        return Result.success(departmentService.create(department));
    }

    /**
     * 更新部门信息
     * @param id 部门ID
     * @param department 更新后的部门信息
     * @return 更新后的部门信息
     */
    @PutMapping("/{id}")
    public Result<Department> update(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        return Result.success(departmentService.update(department));
    }

    /**
     * 删除部门
     * @param id 部门ID
     * @return 成功返回空结果；如果部门有用户或子部门则返回错误
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!departmentService.canDelete(id)) {
            return Result.error("Cannot delete: department has users or children");
        }
        departmentMapper.physicalDeleteById(id);
        return Result.success();
    }

    /**
     * 获取部门下的用户列表
     * @param id 部门ID
     * @return 用户列表
     */
    @GetMapping("/{id}/users")
    public Result<List<User>> getDepartmentUsers(@PathVariable Long id) {
        return Result.success(departmentMapper.findUsersByDepartmentId(id));
    }

    /**
     * 获取未分配部门的用户列表
     * @return 可用用户列表（未分配到任何部门的用户）
     */
    @GetMapping("/users/available")
    public Result<List<User>> getAvailableUsers() {
        return Result.success(departmentMapper.findUsersWithoutDepartment());
    }

    /**
     * 添加用户到部门
     * @param id 部门ID
     * @param userId 用户ID
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/users/{userId}")
    public Result<Void> addUserToDepartment(@PathVariable Long id, @PathVariable Long userId) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
               .set(User::getDepartmentId, id);
        userMapper.update(null, wrapper);
        return Result.success();
    }

    /**
     * 从部门移除用户
     * @param id 部门ID
     * @param userId 用户ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}/users/{userId}")
    public Result<Void> removeUserFromDepartment(@PathVariable Long id, @PathVariable Long userId) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
               .set(User::getDepartmentId, null);
        userMapper.update(null, wrapper);
        return Result.success();
    }
}
