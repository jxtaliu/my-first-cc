package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

    public UserController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
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

    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        userService.assignRoles(id, roleIds);
        return Result.success();
    }

    @PutMapping("/{id}/department")
    public Result<Void> assignDepartment(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long departmentId = body.get("departmentId");
        userService.assignDepartment(id, departmentId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }
}
