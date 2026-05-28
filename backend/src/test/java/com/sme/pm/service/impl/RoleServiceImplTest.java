package com.sme.pm.service.impl;

import com.sme.pm.entity.Permission;
import com.sme.pm.entity.Role;
import com.sme.pm.mapper.PermissionMapper;
import com.sme.pm.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(roleMapper, permissionMapper);
    }

    // ==================== getAllRoles Tests ====================

    @Test
    void getAllRoles_shouldReturnAllRoles() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setRoleId("ROLE_001");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setRoleId("ROLE_002");

        when(roleMapper.findAll()).thenReturn(Arrays.asList(role1, role2));

        List<Role> result = roleService.getAllRoles();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roleMapper).findAll();
    }

    @Test
    void getAllRoles_shouldReturnEmptyList_whenNoRoles() {
        when(roleMapper.findAll()).thenReturn(Collections.emptyList());

        List<Role> result = roleService.getAllRoles();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getById Tests ====================

    @Test
    void getById_shouldReturnRole() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleId("ROLE_001");

        when(roleMapper.selectById(1L)).thenReturn(role);

        Role result = roleService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(roleMapper).selectById(1L);
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(roleMapper.selectById(999L)).thenReturn(null);

        Role result = roleService.getById(999L);

        assertNull(result);
    }

    // ==================== create Tests ====================

    @Test
    void create_shouldGenerateRoleId() {
        Role role = new Role();
        role.setName("Test Role");

        when(roleMapper.getMaxRoleIdNumber()).thenReturn(5L);
        when(roleMapper.insert(any(Role.class))).thenReturn(1);

        Role result = roleService.create(role);

        assertNotNull(result);
        assertEquals("ROLE_006", result.getRoleId());
        verify(roleMapper).insert(role);
    }

    // Note: update(Role) method uses LambdaUpdateWrapper which requires MyBatis-Plus entity metadata
    // LambdaUpdateWrapper cannot be properly mocked in unit tests
    // The update method is tested via integration tests

    // ==================== delete Tests ====================

    @Test
    void delete_shouldCallMapper() {
        Long id = 1L;

        roleService.delete(id);

        verify(roleMapper).deleteById(id);
    }

    // ==================== getPermissions Tests ====================

    @Test
    void getPermissions_shouldReturnPermissions() {
        Long roleId = 1L;
        Permission perm1 = new Permission();
        perm1.setId(1L);

        Permission perm2 = new Permission();
        perm2.setId(2L);

        when(permissionMapper.findByRoleId(roleId)).thenReturn(Arrays.asList(perm1, perm2));

        List<Permission> result = roleService.getPermissions(roleId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(permissionMapper).findByRoleId(roleId);
    }

    @Test
    void getPermissions_shouldReturnEmptyList_whenNoPermissions() {
        Long roleId = 1L;

        when(permissionMapper.findByRoleId(roleId)).thenReturn(Collections.emptyList());

        List<Permission> result = roleService.getPermissions(roleId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== assignPermissions Tests ====================

    @Test
    void assignPermissions_shouldDeleteAndReassign() {
        Long roleId = 1L;
        List<Long> permissionIds = Arrays.asList(1L, 2L, 3L);

        roleService.assignPermissions(roleId, permissionIds);

        verify(roleMapper).deleteRolePermissions(roleId);
        verify(roleMapper, times(3)).insertRolePermission(eq(roleId), any(Long.class));
    }

    @Test
    void assignPermissions_shouldHandleEmptyList() {
        Long roleId = 1L;
        List<Long> permissionIds = Collections.emptyList();

        roleService.assignPermissions(roleId, permissionIds);

        verify(roleMapper).deleteRolePermissions(roleId);
        verify(roleMapper, never()).insertRolePermission(anyLong(), anyLong());
    }

    // ==================== canDelete Tests ====================

    @Test
    void canDelete_shouldReturnTrue_whenNoUsers() {
        Long roleId = 1L;
        when(roleMapper.countUsersByRoleId(roleId)).thenReturn(0);

        boolean result = roleService.canDelete(roleId);

        assertTrue(result);
    }

    @Test
    void canDelete_shouldReturnFalse_whenUsersExist() {
        Long roleId = 1L;
        when(roleMapper.countUsersByRoleId(roleId)).thenReturn(5);

        boolean result = roleService.canDelete(roleId);

        assertFalse(result);
    }
}
