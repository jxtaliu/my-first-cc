package com.sme.pm.service;

import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;

public interface UserService {
    LoginResponse login(LoginRequest request);
    User register(User user);
    User getUserById(Long id);
}
