package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.dto.DepartmentAssignRequest;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/me")
    public Result<User> getCurrentUser(@CurrentUser Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userMapper.selectList(null));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        List<Long> roleIds = userMapper.findRolesByUserId(id).stream()
                .map(role -> role.getId())
                .toList();
        return Result.success(roleIds);
    }

    @PostMapping
    public Result<User> create(@RequestBody User user) {
        return Result.success(userService.register(user));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody RoleAssignRequest body) {
        userService.assignRoles(id, body.getRoleIds());
        return Result.success();
    }

    @PutMapping("/{id}/department")
    public Result<Void> assignDepartment(@PathVariable Long id, @RequestBody DepartmentAssignRequest body) {
        userService.assignDepartment(id, body.getDepartmentId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userMapper.deleteUserRolesByUserId(id);
        userMapper.physicalDeleteById(id);
        return Result.success();
    }
}
