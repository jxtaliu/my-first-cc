package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;
import com.sme.pm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody User user) {
        return Result.success(userService.register(user));
    }
}
