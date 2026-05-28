package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Project;
import com.sme.pm.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectController.
 *
 * <p>Test scenarios covered:</p>
 * <ul>
 *   <li>Create project</li>
 *   <li>Get project by ID</li>
 *   <li>Update project</li>
 *   <li>Delete project</li>
 *   <li>List all projects</li>
 *   <li>List projects by status</li>
 *   <li>Archive project</li>
 *   <li>Restore project</li>
 *   <li>Get project members</li>
 *   <li>Add project member</li>
 *   <li>Update member role</li>
 *   <li>Remove project member</li>
 *   <li>Get project stats</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        projectController = new ProjectController(projectService);
    }

    // ==================== Create Project Tests ====================

    @Test
    void create_shouldReturnCreatedProject() {
        // Arrange
        Project project = createProject(1L, "Test Project", "PRJ_001");
        when(projectService.create(any(Project.class))).thenReturn(project);

        // Act
        Result<Project> result = projectController.create(project, 1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getId());
        assertEquals(1L, project.getOwnerId());
    }

    // ==================== Get Project By ID Tests ====================

    @Test
    void getById_shouldReturnProject() {
        // Arrange
        Project project = createProject(1L, "Test Project", "PRJ_001");
        when(projectService.getById(1L)).thenReturn(project);

        // Act
        Result<Project> result = projectController.getById(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("Test Project", result.getData().getName());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(projectService.getById(999L)).thenReturn(null);

        // Act
        Result<Project> result = projectController.getById(999L);

        // Assert
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    // ==================== Update Project Tests ====================

    @Test
    void update_shouldReturnUpdatedProject() {
        // Arrange
        Project project = createProject(1L, "Updated Project", "PRJ_001");
        when(projectService.update(any(Project.class))).thenReturn(project);

        // Act
        Result<Project> result = projectController.update(1L, project);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("Updated Project", result.getData().getName());
        assertEquals(1L, project.getId());
    }

    // ==================== Delete Project Tests ====================

    @Test
    void delete_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = projectController.delete(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).delete(1L);
    }

    // ==================== List Projects Tests ====================

    @Test
    void list_shouldReturnAllProjects_whenNoStatusProvided() {
        // Arrange
        List<Project> projects = Arrays.asList(
            createProject(1L, "Project 1", "PRJ_001"),
            createProject(2L, "Project 2", "PRJ_002")
        );
        when(projectService.list()).thenReturn(projects);

        // Act
        Result<List<Project>> result = projectController.list(null);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    @Test
    void list_shouldReturnProjectsByStatus() {
        // Arrange
        List<Project> projects = Collections.singletonList(
            createProject(1L, "Active Project", "PRJ_001")
        );
        when(projectService.listByStatus("ACTIVE")).thenReturn(projects);

        // Act
        Result<List<Project>> result = projectController.list("ACTIVE");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        verify(projectService).listByStatus("ACTIVE");
    }

    @Test
    void list_shouldReturnAllProjects_whenEmptyStatusProvided() {
        // Arrange
        List<Project> projects = Collections.singletonList(
            createProject(1L, "Project 1", "PRJ_001")
        );
        when(projectService.list()).thenReturn(projects);

        // Act
        Result<List<Project>> result = projectController.list("");

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).list();
    }

    // ==================== Archive Project Tests ====================

    @Test
    void archive_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = projectController.archive(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).archive(1L);
    }

    // ==================== Restore Project Tests ====================

    @Test
    void restore_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = projectController.restore(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).restore(1L);
    }

    // ==================== Get Members Tests ====================

    @Test
    void getMembers_shouldReturnMemberList() {
        // Arrange
        List<Map<String, Object>> members = Arrays.asList(
            Map.of("userId", 1L, "role", "DEVELOPER"),
            Map.of("userId", 2L, "role", "MANAGER")
        );
        when(projectService.getMembers("PRJ_001")).thenReturn(members);

        // Act
        Result<?> result = projectController.getMembers("PRJ_001");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, ((List<?>) result.getData()).size());
    }

    // ==================== Add Member Tests ====================

    @Test
    void addMember_shouldCallServiceWithUserIdAsNumber() {
        // Arrange
        Map<String, Object> member = new HashMap<>();
        member.put("userId", 5L);
        member.put("roleId", "ROLE_DEVELOPER");

        // Act
        Result<Void> result = projectController.addMember("PRJ_001", member);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).addMember("PRJ_001", 5L, "ROLE_DEVELOPER");
    }

    @Test
    void addMember_shouldCallServiceWithUserIdAsString() {
        // Arrange
        Map<String, Object> member = new HashMap<>();
        member.put("userId", "10");
        member.put("roleId", "ROLE_MANAGER");

        // Act
        Result<Void> result = projectController.addMember("PRJ_001", member);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).addMember("PRJ_001", 10L, "ROLE_MANAGER");
    }

    @Test
    void addMember_shouldUseDefaultRole_whenRoleNotProvided() {
        // Arrange
        Map<String, Object> member = new HashMap<>();
        member.put("userId", 5L);

        // Act
        Result<Void> result = projectController.addMember("PRJ_001", member);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).addMember("PRJ_001", 5L, "ROLE_DEVELOPER");
    }

    // ==================== Update Member Role Tests ====================

    @Test
    void updateMemberRole_shouldCallServiceAndReturnSuccess() {
        // Arrange
        Map<String, Object> member = new HashMap<>();
        member.put("roleId", "ROLE_ADMIN");

        // Act
        Result<Void> result = projectController.updateMemberRole("PRJ_001", 1L, member);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).updateMemberRole("PRJ_001", 1L, "ROLE_ADMIN");
    }

    // ==================== Remove Member Tests ====================

    @Test
    void removeMember_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = projectController.removeMember("PRJ_001", 1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(projectService).removeMember("PRJ_001", 1L);
    }

    // ==================== Get Stats Tests ====================

    @Test
    void getStats_shouldReturnStatsData() {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", 100);
        stats.put("completedTasks", 50);
        stats.put("memberCount", 10);
        when(projectService.getStats("PRJ_001")).thenReturn(stats);

        // Act
        Result<Object> result = projectController.getStats("PRJ_001");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(100, ((Map<?, ?>) result.getData()).get("totalTasks"));
    }

    // ==================== Helper Methods ====================

    private Project createProject(Long id, String name, String projectId) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setProjectId(projectId);
        project.setStatus("ACTIVE");
        return project;
    }
}
