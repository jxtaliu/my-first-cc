package com.sme.pm.service.impl;

import com.sme.pm.entity.ProjectRole;
import com.sme.pm.mapper.ProjectRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplTest {

    @Mock
    private ProjectRoleMapper projectRoleMapper;

    @InjectMocks
    private RolePermissionServiceImpl rolePermissionService;

    private ProjectRole ownerRole;
    private ProjectRole managerRole;
    private ProjectRole developerRole;
    private ProjectRole devLeadRole;

    @BeforeEach
    void setUp() {
        ownerRole = new ProjectRole();
        ownerRole.setId(1L);
        ownerRole.setProjectId("PRJ_001");
        ownerRole.setUserId(1L);
        ownerRole.setRole("PROJECT_OWNER");

        managerRole = new ProjectRole();
        managerRole.setId(2L);
        managerRole.setProjectId("PRJ_001");
        managerRole.setUserId(2L);
        managerRole.setRole("PROJECT_MANAGER");

        developerRole = new ProjectRole();
        developerRole.setId(3L);
        developerRole.setProjectId("PRJ_001");
        developerRole.setUserId(3L);
        developerRole.setRole("DEVELOPER");

        devLeadRole = new ProjectRole();
        devLeadRole.setId(4L);
        devLeadRole.setProjectId("PRJ_001");
        devLeadRole.setUserId(4L);
        devLeadRole.setRole("DEV_LEAD");
    }

    @Test
    void testCanApproveTimesheet_shouldReturnTrueForManager() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 2L)).thenReturn(managerRole);

        // Act
        boolean result = rolePermissionService.canApproveTimesheet(2L, "PRJ_001");

        // Assert
        assertTrue(result);
        verify(projectRoleMapper).findByProjectAndUser("PRJ_001", 2L);
    }

    @Test
    void testCanApproveTimesheet_shouldReturnTrueForOwner() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 1L)).thenReturn(ownerRole);

        // Act
        boolean result = rolePermissionService.canApproveTimesheet(1L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanApproveTimesheet_shouldReturnFalseForDeveloper() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 3L)).thenReturn(developerRole);

        // Act
        boolean result = rolePermissionService.canApproveTimesheet(3L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanApproveTimesheet_shouldReturnFalse_whenNoRoleFound() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 999L)).thenReturn(null);

        // Act
        boolean result = rolePermissionService.canApproveTimesheet(999L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanManageSprint_shouldReturnTrueForOwner() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 1L)).thenReturn(ownerRole);

        // Act
        boolean result = rolePermissionService.canManageSprint(1L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanManageSprint_shouldReturnTrueForManager() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 2L)).thenReturn(managerRole);

        // Act
        boolean result = rolePermissionService.canManageSprint(2L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanManageSprint_shouldReturnFalseForDeveloper() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 3L)).thenReturn(developerRole);

        // Act
        boolean result = rolePermissionService.canManageSprint(3L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanManageSprint_shouldReturnFalseForDevLead() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 4L)).thenReturn(devLeadRole);

        // Act
        boolean result = rolePermissionService.canManageSprint(4L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanManageSprint_shouldReturnFalse_whenNoRoleFound() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 999L)).thenReturn(null);

        // Act
        boolean result = rolePermissionService.canManageSprint(999L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetProjectRole_shouldReturnCorrectRole() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 1L)).thenReturn(ownerRole);

        // Act
        String role = rolePermissionService.getProjectRole(1L, "PRJ_001");

        // Assert
        assertEquals("PROJECT_OWNER", role);
    }

    @Test
    void testGetProjectRole_shouldReturnCorrectRoleForManager() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 2L)).thenReturn(managerRole);

        // Act
        String role = rolePermissionService.getProjectRole(2L, "PRJ_001");

        // Assert
        assertEquals("PROJECT_MANAGER", role);
    }

    @Test
    void testGetProjectRole_shouldReturnCorrectRoleForDeveloper() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 3L)).thenReturn(developerRole);

        // Act
        String role = rolePermissionService.getProjectRole(3L, "PRJ_001");

        // Assert
        assertEquals("DEVELOPER", role);
    }

    @Test
    void testGetProjectRole_shouldReturnNone_whenNoRoleFound() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 999L)).thenReturn(null);

        // Act
        String role = rolePermissionService.getProjectRole(999L, "PRJ_001");

        // Assert
        assertEquals("NONE", role);
    }

    @Test
    void testCanManageTasks_shouldReturnTrueForOwner() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 1L)).thenReturn(ownerRole);

        // Act
        boolean result = rolePermissionService.canManageTasks(1L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanManageTasks_shouldReturnTrueForManager() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 2L)).thenReturn(managerRole);

        // Act
        boolean result = rolePermissionService.canManageTasks(2L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanManageTasks_shouldReturnTrueForDevLead() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 4L)).thenReturn(devLeadRole);

        // Act
        boolean result = rolePermissionService.canManageTasks(4L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanManageTasks_shouldReturnFalseForDeveloper() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 3L)).thenReturn(developerRole);

        // Act
        boolean result = rolePermissionService.canManageTasks(3L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanManageMembers_shouldReturnTrueForOwner() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 1L)).thenReturn(ownerRole);

        // Act
        boolean result = rolePermissionService.canManageMembers(1L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanManageMembers_shouldReturnTrueForManager() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 2L)).thenReturn(managerRole);

        // Act
        boolean result = rolePermissionService.canManageMembers(2L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanManageMembers_shouldReturnFalseForDevLead() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 4L)).thenReturn(devLeadRole);

        // Act
        boolean result = rolePermissionService.canManageMembers(4L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanConfigureProject_shouldReturnTrueForOwner() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 1L)).thenReturn(ownerRole);

        // Act
        boolean result = rolePermissionService.canConfigureProject(1L, "PRJ_001");

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanConfigureProject_shouldReturnFalseForManager() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 2L)).thenReturn(managerRole);

        // Act
        boolean result = rolePermissionService.canConfigureProject(2L, "PRJ_001");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanConfigureProject_shouldReturnFalseForDevLead() {
        // Arrange
        when(projectRoleMapper.findByProjectAndUser("PRJ_001", 4L)).thenReturn(devLeadRole);

        // Act
        boolean result = rolePermissionService.canConfigureProject(4L, "PRJ_001");

        // Assert
        assertFalse(result);
    }
}
