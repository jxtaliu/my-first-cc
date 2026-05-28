package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.ProjectRole;
import com.sme.pm.mapper.ProjectRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectRoleServiceImplTest {

    @Mock
    private ProjectRoleMapper projectRoleMapper;

    private ProjectRoleServiceImpl projectRoleService;

    @BeforeEach
    void setUp() throws Exception {
        projectRoleService = new ProjectRoleServiceImpl();

        // Inject mock baseMapper using reflection
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(projectRoleService, projectRoleMapper);
    }

    // ==================== findByProjectId Tests ====================

    @Test
    void findByProjectId_shouldReturnRoles() {
        String projectId = "PRJ001";
        ProjectRole role1 = new ProjectRole();
        role1.setId(1L);
        role1.setProjectId(projectId);
        role1.setUserId(100L);
        role1.setRole("DEVELOPER");

        when(projectRoleMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(role1));

        List<ProjectRole> result = projectRoleService.findByProjectId(projectId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DEVELOPER", result.get(0).getRole());
    }

    @Test
    void findByProjectId_shouldReturnEmptyList_whenNoRoles() {
        when(projectRoleMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        List<ProjectRole> result = projectRoleService.findByProjectId("PRJ999");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== findByUserId Tests ====================

    @Test
    void findByUserId_shouldReturnRoles() {
        Long userId = 100L;
        ProjectRole role1 = new ProjectRole();
        role1.setId(1L);
        role1.setProjectId("PRJ001");
        role1.setUserId(userId);
        role1.setRole("DEVELOPER");

        when(projectRoleMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(role1));

        List<ProjectRole> result = projectRoleService.findByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findByUserId_shouldReturnEmptyList_whenNoRoles() {
        when(projectRoleMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        List<ProjectRole> result = projectRoleService.findByUserId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== findByProjectAndUser Tests ====================

    @Test
    void findByProjectAndUser_shouldReturnRole_whenExists() {
        String projectId = "PRJ001";
        Long userId = 100L;
        ProjectRole role = new ProjectRole();
        role.setId(1L);
        role.setProjectId(projectId);
        role.setUserId(userId);
        role.setRole("LEAD");

        // Mock selectOne with two args (QueryWrapper, throwEx)
        when(projectRoleMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class), eq(true)))
            .thenReturn(role);

        ProjectRole result = projectRoleService.findByProjectAndUser(projectId, userId);

        assertNotNull(result);
        assertEquals("LEAD", result.getRole());
    }

    @Test
    void findByProjectAndUser_shouldReturnNull_whenNotExists() {
        when(projectRoleMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class), eq(true)))
            .thenReturn(null);

        ProjectRole result = projectRoleService.findByProjectAndUser("PRJ999", 999L);

        assertNull(result);
    }

    // ==================== findByProjectAndRole Tests ====================

    @Test
    void findByProjectAndRole_shouldReturnRoles() {
        String projectId = "PRJ001";
        String role = "DEVELOPER";
        ProjectRole role1 = new ProjectRole();
        role1.setId(1L);
        role1.setProjectId(projectId);
        role1.setUserId(100L);
        role1.setRole(role);

        ProjectRole role2 = new ProjectRole();
        role2.setId(2L);
        role2.setProjectId(projectId);
        role2.setUserId(101L);
        role2.setRole(role);

        when(projectRoleMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(role1, role2));

        List<ProjectRole> result = projectRoleService.findByProjectAndRole(projectId, role);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== assignRole Tests ====================

    @Test
    void assignRole_shouldUpdateExistingRole_whenRoleAlreadyAssigned() {
        String projectId = "PRJ001";
        Long userId = 100L;
        String newRole = "LEAD";

        ProjectRole existingRole = new ProjectRole();
        existingRole.setId(1L);
        existingRole.setProjectId(projectId);
        existingRole.setUserId(userId);
        existingRole.setRole("DEVELOPER");

        // Mock findByProjectAndUser to return existing role
        when(projectRoleMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class), eq(true)))
            .thenReturn(existingRole);
        when(projectRoleMapper.updateById(any(ProjectRole.class))).thenReturn(1);

        projectRoleService.assignRole(projectId, userId, newRole);

        verify(projectRoleMapper).updateById(existingRole);
        assertEquals(newRole, existingRole.getRole());
        verify(projectRoleMapper, never()).insert(any(ProjectRole.class));
    }

    @Test
    void assignRole_shouldCreateNewRole_whenNoExistingRole() {
        String projectId = "PRJ001";
        Long userId = 100L;
        String newRole = "DEVELOPER";

        // Mock findByProjectAndUser to return null (no existing role)
        when(projectRoleMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class), eq(true)))
            .thenReturn(null);
        when(projectRoleMapper.insert(any(ProjectRole.class))).thenReturn(1);

        projectRoleService.assignRole(projectId, userId, newRole);

        verify(projectRoleMapper).insert(any(ProjectRole.class));
        verify(projectRoleMapper, never()).updateById(any(ProjectRole.class));
    }

    // ==================== removeRole Tests ====================

    @Test
    void removeRole_shouldRemoveRole() {
        String projectId = "PRJ001";
        Long userId = 100L;

        projectRoleService.removeRole(projectId, userId);

        verify(projectRoleMapper).delete(any(LambdaQueryWrapper.class));
    }
}
