package com.sme.pm.service;

import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;
import java.util.List;

public interface UserService {
    LoginResponse login(LoginRequest request);
    User register(User user);
    User getUserById(Long id);
    void assignRoles(Long userId, List<Long> roleIds);
    void assignDepartment(Long userId, Long departmentId);
}
