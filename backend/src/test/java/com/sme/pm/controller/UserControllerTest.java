package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Role;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController.
 *
 * <p>Test scenarios covered:</p>
 * <ul>
 *   <li>Get current user info</li>
 *   <li>Get all users</li>
 *   <li>Get user by ID</li>
 *   <li>Get user roles</li>
 *   <li>Create new user</li>
 *   <li>Update user</li>
 *   <li>Assign roles to user</li>
 *   <li>Assign department to user</li>
 *   <li>Remove department from user</li>
 *   <li>Delete user</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userMapper, userService, passwordEncoder);
    }

    // ==================== Get Current User Tests ====================

    @Test
    void getCurrentUser_shouldReturnUserWithoutPassword() {
        // Arrange
        User user = createUser(1L, "admin", "Admin User");
        user.setPassword("secret");
        when(userMapper.selectById(1L)).thenReturn(user);

        // Act
        Result<User> result = userController.getCurrentUser(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertNull(result.getData().getPassword());
        assertEquals("admin", result.getData().getUsername());
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenUserNotFound() {
        // Arrange
        when(userMapper.selectById(999L)).thenReturn(null);

        // Act
        Result<User> result = userController.getCurrentUser(999L);

        // Assert
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    // ==================== Get All Users Tests ====================

    @Test
    void getAllUsers_shouldReturnUserList() {
        // Arrange
        User user1 = createUser(1L, "admin", "Admin");
        User user2 = createUser(2L, "user1", "User One");
        when(userMapper.selectList(null)).thenReturn(Arrays.asList(user1, user2));

        // Act
        Result<List<User>> result = userController.getAllUsers();

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    @Test
    void getAllUsers_shouldReturnEmptyList() {
        // Arrange
        when(userMapper.selectList(null)).thenReturn(Collections.emptyList());

        // Act
        Result<List<User>> result = userController.getAllUsers();

        // Assert
        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ==================== Get User By ID Tests ====================

    @Test
    void getUser_shouldReturnUser() {
        // Arrange
        User user = createUser(1L, "admin", "Admin User");
        when(userMapper.selectById(1L)).thenReturn(user);

        // Act
        Result<User> result = userController.getUser(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("admin", result.getData().getUsername());
    }

    @Test
    void getUser_shouldReturnNull_whenNotFound() {
        // Arrange
        when(userMapper.selectById(999L)).thenReturn(null);

        // Act
        Result<User> result = userController.getUser(999L);

        // Assert
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    // ==================== Get User Roles Tests ====================

    @Test
    void getUserRoles_shouldReturnRoleIds() {
        // Arrange
        Role role1 = new Role();
        role1.setId(1L);
        Role role2 = new Role();
        role2.setId(2L);
        when(userMapper.findRolesByUserId(1L)).thenReturn(Arrays.asList(role1, role2));

        // Act
        Result<List<Long>> result = userController.getUserRoles(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
        assertTrue(result.getData().contains(1L));
        assertTrue(result.getData().contains(2L));
    }

    @Test
    void getUserRoles_shouldReturnEmptyList_whenNoRoles() {
        // Arrange
        when(userMapper.findRolesByUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        Result<List<Long>> result = userController.getUserRoles(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ==================== Create User Tests ====================

    @Test
    void create_shouldReturnCreatedUser() {
        // Arrange
        User user = createUser(null, "newuser", "New User");
        User createdUser = createUser(1L, "newuser", "New User");
        when(userService.register(any(User.class))).thenReturn(createdUser);

        // Act
        Result<User> result = userController.create(user);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getId());
        verify(userService).register(user);
    }

    // ==================== Update User Tests ====================

    @Test
    void update_shouldUpdateUserWithoutPasswordChange() {
        // Arrange
        User user = createUser(1L, "admin", "Updated Admin");
        user.setPassword(null);

        // Act
        Result<Void> result = userController.update(1L, user);

        // Assert
        assertEquals(200, result.getCode());
        verify(userMapper).updateById(user);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_shouldEncodePassword_whenPasswordProvided() {
        // Arrange
        User user = createUser(1L, "admin", "Admin");
        user.setPassword("newpassword");
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded_password");

        // Act
        Result<Void> result = userController.update(1L, user);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("encoded_password", user.getPassword());
        verify(passwordEncoder).encode("newpassword");
        verify(userMapper).updateById(user);
    }

    // ==================== Assign Roles Tests ====================

    @Test
    void assignRoles_shouldCallServiceWithRoleIds() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("roleIds", Arrays.asList(1, 2, 3));

        // Act
        Result<Void> result = userController.assignRoles(1L, body);

        // Assert
        assertEquals(200, result.getCode());
        verify(userService).assignRoles(eq(1L), any());
    }

    @Test
    void assignRoles_shouldHandleNullRoleIds() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("roleIds", null);

        // Act
        Result<Void> result = userController.assignRoles(1L, body);

        // Assert
        assertEquals(200, result.getCode());
        verify(userService).assignRoles(eq(1L), isNull());
    }

    // ==================== Assign Department Tests ====================

    @Test
    void assignDepartment_shouldCallServiceWithDepartmentId() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("departmentId", 5L);

        // Act
        Result<Void> result = userController.assignDepartment(1L, body);

        // Assert
        assertEquals(200, result.getCode());
        verify(userService).assignDepartment(1L, 5L);
    }

    @Test
    void assignDepartment_shouldHandleNullDepartmentId() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("departmentId", null);

        // Act
        Result<Void> result = userController.assignDepartment(1L, body);

        // Assert
        assertEquals(200, result.getCode());
        verify(userService).assignDepartment(1L, null);
    }

    @Test
    void assignDepartment_shouldHandleStringDepartmentId() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("departmentId", "10");

        // Act
        Result<Void> result = userController.assignDepartment(1L, body);

        // Assert
        assertEquals(200, result.getCode());
        verify(userService).assignDepartment(1L, 10L);
    }

    // ==================== Remove Department Tests ====================

    @Test
    void removeDepartment_shouldAssignNullToService() {
        // Act
        Result<Void> result = userController.removeDepartment(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(userService).assignDepartment(1L, null);
    }

    // ==================== Delete User Tests ====================

    @Test
    void deleteUser_shouldDeleteUserAndRoles() {
        // Act
        Result<Void> result = userController.deleteUser(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(userMapper).deleteUserRolesByUserId(1L);
        verify(userMapper).physicalDeleteById(1L);
    }

    // ==================== Helper Methods ====================

    private User createUser(Long id, String username, String realName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setPassword(null);
        return user;
    }
}
