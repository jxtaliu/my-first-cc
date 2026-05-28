package com.sme.pm.service.impl;

import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.security.JwtTokenProvider;
import com.sme.pm.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder, tokenProvider);
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setRealName("Admin User");

        when(userMapper.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(1L, "admin")).thenReturn("jwt-token-123");

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("admin", response.getUsername());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encodedPassword");

        when(userMapper.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }

    @Test
    void register_shouldCreateUser_whenUsernameNotExists() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");
        user.setEmail("new@example.com");

        when(userMapper.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        User result = userService.register(user);

        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        verify(userMapper).insert(user);
    }

    @Test
    void register_shouldThrowException_whenUsernameExists() {
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("password123");

        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userMapper.findByUsername("existinguser")).thenReturn(existingUser);

        assertThrows(IllegalArgumentException.class, () -> userService.register(user));
    }

    @Test
    void getUserById_shouldReturnUserWithoutPassword() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("admin");
        user.setPassword("secretPassword");
        user.setRealName("Admin");

        when(userMapper.selectById(userId)).thenReturn(user);

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertNull(result.getPassword()); // Password should be null
    }

    @Test
    void getUserById_shouldReturnNull_whenUserNotFound() {
        Long userId = 999L;

        when(userMapper.selectById(userId)).thenReturn(null);

        User result = userService.getUserById(userId);

        assertNull(result);
    }
}
