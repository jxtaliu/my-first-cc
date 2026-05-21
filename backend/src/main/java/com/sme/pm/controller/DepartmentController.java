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

    @GetMapping
    public Result<List<Department>> getAll() {
        return Result.success(departmentService.getAllDepartments());
    }

    @GetMapping("/tree")
    public Result<List<Department>> getTree() {
        return Result.success(departmentService.getDepartmentTree());
    }

    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        Department dept = departmentService.getById(id);
        return dept == null ? Result.success(null) : Result.success(dept);
    }

    @PostMapping
    public Result<Department> create(@RequestBody Department department) {
        return Result.success(departmentService.create(department));
    }

    @PutMapping("/{id}")
    public Result<Department> update(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        return Result.success(departmentService.update(department));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!departmentService.canDelete(id)) {
            return Result.error("Cannot delete: department has users or children");
        }
        departmentMapper.physicalDeleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}/users")
    public Result<List<User>> getDepartmentUsers(@PathVariable Long id) {
        return Result.success(departmentMapper.findUsersByDepartmentId(id));
    }

    @GetMapping("/users/available")
    public Result<List<User>> getAvailableUsers() {
        return Result.success(departmentMapper.findUsersWithoutDepartment());
    }

    @PostMapping("/{id}/users/{userId}")
    public Result<Void> addUserToDepartment(@PathVariable Long id, @PathVariable Long userId) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
               .set(User::getDepartmentId, id);
        userMapper.update(null, wrapper);
        return Result.success();
    }

    @DeleteMapping("/{id}/users/{userId}")
    public Result<Void> removeUserFromDepartment(@PathVariable Long id, @PathVariable Long userId) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
               .set(User::getDepartmentId, null);
        userMapper.update(null, wrapper);
        return Result.success();
    }
}
