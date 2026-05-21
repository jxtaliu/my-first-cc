package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@CurrentUser Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }
}
