package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.ProjectService;
import com.sme.pm.service.ITaskStatusService;
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

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ITaskStatusService taskStatusService;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(projectMapper, taskMapper, taskStatusService);
    }

    // ==================== Create Tests ====================

    @Test
    void create_shouldThrowException_whenProjectIdEmpty() {
        Project project = new Project();
        project.setName("Test");
        project.setOwnerId(1L);

        assertThrows(IllegalArgumentException.class, () -> projectService.create(project));
    }

    @Test
    void create_shouldThrowException_whenNameEmpty() {
        Project project = new Project();
        project.setProjectId("PRJ001");
        project.setOwnerId(1L);

        assertThrows(IllegalArgumentException.class, () -> projectService.create(project));
    }

    @Test
    void create_shouldThrowException_whenOwnerIdNull() {
        Project project = new Project();
        project.setProjectId("PRJ001");
        project.setName("Test");

        assertThrows(IllegalArgumentException.class, () -> projectService.create(project));
    }

    @Test
    void create_shouldThrowException_whenDuplicateProjectId() {
        Project project = new Project();
        project.setProjectId("PRJ001");
        project.setName("Test");
        project.setOwnerId(1L);

        when(projectMapper.findByProjectId("PRJ001")).thenReturn(new Project());

        assertThrows(IllegalArgumentException.class, () -> projectService.create(project));
    }

    @Test
    void create_shouldSucceed_whenValidProject() {
        Project project = new Project();
        project.setProjectId("PRJ001");
        project.setName("Test");
        project.setOwnerId(1L);

        when(projectMapper.findByProjectId("PRJ001")).thenReturn(null);
        when(projectMapper.insert(any(Project.class))).thenReturn(1);
        doNothing().when(taskStatusService).initializeFromDict(anyString());

        Project result = projectService.create(project);

        assertNotNull(result);
        assertEquals("PRJ001", result.getProjectId());
        verify(projectMapper).insert(project);
        verify(taskStatusService).initializeFromDict("PRJ001");
    }

    // ==================== Update Tests ====================

    @Test
    void update_shouldPreserveProjectId_whenNotProvided() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Updated Name");

        Project existing = new Project();
        existing.setId(1L);
        existing.setProjectId("PRJ001");

        when(projectMapper.findById(1L)).thenReturn(existing);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        Project result = projectService.update(project);

        assertEquals("PRJ001", result.getProjectId());
    }

    @Test
    void update_shouldUseProvidedProjectId_whenGiven() {
        Project project = new Project();
        project.setId(1L);
        project.setProjectId("PRJ002");
        project.setName("Updated Name");

        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        Project result = projectService.update(project);

        assertEquals("PRJ002", result.getProjectId());
    }

    // ==================== Get/Find Tests ====================

    @Test
    void getById_shouldPopulateStats() {
        Project project = new Project();
        project.setId(1L);
        project.setProjectId("PRJ001");

        when(projectMapper.findById(1L)).thenReturn(project);
        when(projectMapper.findMemberIds("PRJ001")).thenReturn(Arrays.asList(1L, 2L));
        when(taskMapper.countByProjectIdAndType("PRJ001", "TASK")).thenReturn(10);
        when(taskMapper.countByProjectIdAndStatus("PRJ001", "DONE")).thenReturn(5);
        when(taskMapper.countByProjectIdAndType("PRJ001", "EPIC")).thenReturn(2);
        when(taskMapper.countByProjectIdAndType("PRJ001", "FEATURE")).thenReturn(3);
        when(taskMapper.countByProjectIdAndType("PRJ001", "STORY")).thenReturn(4);
        when(taskMapper.countByProjectIdAndType("PRJ001", "BUG")).thenReturn(1);
        when(taskMapper.countByProjectIdAndType("PRJ001", "SUBTASK")).thenReturn(6);

        Project result = projectService.getById(1L);

        assertNotNull(result);
        assertEquals(2, result.getMemberCount());
        assertEquals(10, result.getTaskCount());
        assertEquals(5, result.getCompletedTaskCount());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(projectMapper.findById(999L)).thenReturn(null);

        Project result = projectService.getById(999L);

        assertNull(result);
    }

    @Test
    void list_shouldPopulateStats() {
        Project project1 = new Project();
        project1.setId(1L);
        project1.setProjectId("PRJ001");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setProjectId("PRJ002");

        when(projectMapper.findAll()).thenReturn(Arrays.asList(project1, project2));
        when(projectMapper.findMemberIds(anyString())).thenReturn(Collections.emptyList());
        when(taskMapper.countByProjectIdAndType(anyString(), anyString())).thenReturn(0);
        when(taskMapper.countByProjectIdAndStatus(anyString(), anyString())).thenReturn(0);

        List<Project> result = projectService.list();

        assertEquals(2, result.size());
    }

    @Test
    void listByStatus_shouldReturnFilteredProjects() {
        Project project1 = new Project();
        project1.setId(1L);
        project1.setProjectId("PRJ001");
        project1.setStatus("ACTIVE");

        when(projectMapper.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(project1));
        when(projectMapper.findMemberIds(anyString())).thenReturn(Collections.emptyList());
        when(taskMapper.countByProjectIdAndType(anyString(), anyString())).thenReturn(0);
        when(taskMapper.countByProjectIdAndStatus(anyString(), anyString())).thenReturn(0);

        List<Project> result = projectService.listByStatus("ACTIVE");

        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    // ==================== Member Management Tests ====================

    @Test
    void addMember_shouldThrowException_whenAlreadyMember() {
        when(projectMapper.findMemberIds("PRJ001")).thenReturn(Arrays.asList(1L, 2L));

        assertThrows(IllegalArgumentException.class, () -> projectService.addMember("PRJ001", 1L, "DEVELOPER"));
    }

    @Test
    void addMember_shouldSucceed_whenNewMember() {
        when(projectMapper.findMemberIds("PRJ001")).thenReturn(Arrays.asList(2L));
        doNothing().when(projectMapper).addMember(anyString(), anyLong(), anyString());

        assertDoesNotThrow(() -> projectService.addMember("PRJ001", 1L, "DEVELOPER"));
        verify(projectMapper).addMember("PRJ001", 1L, "DEVELOPER");
    }

    // ==================== Archive/Restore Tests ====================

    @Test
    void archive_shouldSetStatusToArchived() {
        Long id = 1L;

        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        projectService.archive(id);

        verify(projectMapper).updateById(argThat(project ->
            project.getId().equals(id) && "ARCHIVED".equals(project.getStatus())
        ));
    }

    @Test
    void restore_shouldSetStatusToActive() {
        Long id = 1L;

        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        projectService.restore(id);

        verify(projectMapper).updateById(argThat(project ->
            project.getId().equals(id) && "ACTIVE".equals(project.getStatus())
        ));
    }

    // ==================== getStats Tests ====================

    @Test
    void getStats_shouldReturnEmptyStats_whenNoData() {
        String projectId = "PRJ001";

        Map<String, Object> result = projectService.getStats(projectId);

        assertNotNull(result);
        assertEquals(0, result.get("totalTasks"));
        assertEquals(0, result.get("completedTasks"));
        assertEquals(0, result.get("totalHours"));
    }

    // ==================== getMembers Tests ====================

    @Test
    void getMembers_shouldReturnMemberList() {
        String projectId = "PRJ001";
        Map<String, Object> member1 = new HashMap<>();
        member1.put("userId", 1L);
        member1.put("role", "DEVELOPER");

        when(projectMapper.findMembersByProjectId(projectId))
            .thenReturn(Collections.singletonList(member1));

        List<Map<String, Object>> result = projectService.getMembers(projectId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("userId"));
    }

    @Test
    void getMembers_shouldReturnEmptyList_whenNoMembers() {
        String projectId = "PRJ_NO_MEMBERS";

        when(projectMapper.findMembersByProjectId(projectId))
            .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = projectService.getMembers(projectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== updateMemberRole Tests ====================

    @Test
    void updateMemberRole_shouldCallMapper() {
        String projectId = "PRJ001";
        Long userId = 1L;
        String roleId = "LEAD";

        doNothing().when(projectMapper).updateMemberRole(projectId, userId, roleId);

        assertDoesNotThrow(() -> projectService.updateMemberRole(projectId, userId, roleId));
        verify(projectMapper).updateMemberRole(projectId, userId, roleId);
    }

    // ==================== Additional Branch Coverage ====================

    @Test
    void create_shouldHandleNullProjectId_whenEmptyString() {
        Project project = new Project();
        project.setProjectId("");
        project.setName("Test");
        project.setOwnerId(1L);

        assertThrows(IllegalArgumentException.class, () -> projectService.create(project));
    }

    @Test
    void create_shouldAcceptBlankName_whenWhitespaceOnly() {
        // Note: The current implementation only checks isEmpty(), not whitespace
        // So "   " (whitespace only) is accepted by the validation
        Project project = new Project();
        project.setProjectId("PRJ001");
        project.setName("   ");
        project.setOwnerId(1L);

        // whitespace-only name passes current validation but will fail at DB level
        when(projectMapper.findByProjectId("PRJ001")).thenReturn(null);
        when(projectMapper.insert(any(Project.class))).thenReturn(1);
        doNothing().when(taskStatusService).initializeFromDict(anyString());

        Project result = projectService.create(project);
        assertNotNull(result);
        assertEquals("PRJ001", result.getProjectId());
    }

    @Test
    void update_shouldHandleNullProjectId() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Updated Name");
        // projectId is null

        Project existing = new Project();
        existing.setId(1L);
        existing.setProjectId("PRJ001");

        when(projectMapper.findById(1L)).thenReturn(existing);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        Project result = projectService.update(project);

        assertEquals("PRJ001", result.getProjectId());
        verify(projectMapper).findById(1L);
    }

    @Test
    void getById_shouldCallFindById_whenIdProvided() {
        Project project = new Project();
        project.setId(1L);
        project.setProjectId("PRJ001");

        when(projectMapper.findById(1L)).thenReturn(project);
        when(projectMapper.findMemberIds("PRJ001")).thenReturn(Collections.emptyList());
        when(taskMapper.countByProjectIdAndType(anyString(), anyString())).thenReturn(0);
        when(taskMapper.countByProjectIdAndStatus(anyString(), anyString())).thenReturn(0);

        projectService.getById(1L);

        verify(projectMapper).findById(1L);
    }

    @Test
    void delete_shouldCallMapper() {
        Long id = 1L;

        projectService.delete(id);

        verify(projectMapper).deleteById(id);
    }

    @Test
    void removeMember_shouldCallMapper() {
        String projectId = "PRJ001";
        Long userId = 1L;

        projectService.removeMember(projectId, userId);

        verify(projectMapper).removeMember(projectId, userId);
    }
}
