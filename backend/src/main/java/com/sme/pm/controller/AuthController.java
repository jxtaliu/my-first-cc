package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;
import com.sme.pm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户登录、注册等认证相关API
 *
 * 用法：
 * - POST /api/auth/login - 用户登录，返回JWT令牌
 * - POST /api/auth/register - 用户注册
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     * @param request 包含用户名和密码的登录请求
     * @return 登录成功返回JWT令牌和用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 用户注册
     * @param user 注册用户信息（用户名、密码、邮箱等）
     * @return 注册成功返回用户信息
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody User user) {
        return Result.success(userService.register(user));
    }
}
