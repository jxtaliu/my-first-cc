package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.ISprintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SprintController.
 *
 * <p>Test scenarios covered:</p>
 * <ul>
 *   <li>Create sprint</li>
 *   <li>List sprints by project</li>
 *   <li>Get sprint by ID</li>
 *   <li>Update sprint</li>
 *   <li>Delete sprint</li>
 *   <li>Start sprint</li>
 *   <li>Complete sprint</li>
 *   <li>Get sprint tasks</li>
 *   <li>Get sprint stats</li>
 *   <li>Calculate velocity</li>
 *   <li>Get backlog tasks</li>
 *   <li>Add task to sprint</li>
 *   <li>Remove task from sprint</li>
 *   <li>Batch add tasks</li>
 *   <li>Batch remove tasks</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class SprintControllerTest {

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ISprintService sprintService;

    private SprintController sprintController;

    @BeforeEach
    void setUp() {
        sprintController = new SprintController(sprintMapper, taskMapper, sprintService);
    }

    // ==================== Create Sprint Tests ====================

    @Test
    void create_shouldReturnCreatedSprint() {
        // Arrange
        Sprint sprint = createSprint(1L, "Sprint 1", "PRJ_001");
        when(sprintService.create(any(Sprint.class))).thenReturn(sprint);

        // Act
        Result<Sprint> result = sprintController.create("PRJ_001", sprint);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("PRJ_001", sprint.getProjectId());
        verify(sprintService).create(sprint);
    }

    // ==================== List Sprints Tests ====================

    @Test
    void list_shouldReturnSprintsByProject() {
        // Arrange
        List<Sprint> sprints = Arrays.asList(
            createSprint(1L, "Sprint 1", "PRJ_001"),
            createSprint(2L, "Sprint 2", "PRJ_001")
        );
        when(sprintMapper.findByProjectId("PRJ_001")).thenReturn(sprints);

        // Act
        Result<List<Sprint>> result = sprintController.list("PRJ_001");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    @Test
    void list_shouldReturnEmptyList() {
        // Arrange
        when(sprintMapper.findByProjectId("PRJ_999")).thenReturn(Collections.emptyList());

        // Act
        Result<List<Sprint>> result = sprintController.list("PRJ_999");

        // Assert
        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ==================== Get Sprint By ID Tests ====================

    @Test
    void getById_shouldReturnSprint() {
        // Arrange
        Sprint sprint = createSprint(1L, "Sprint 1", "PRJ_001");
        when(sprintMapper.findById(1L)).thenReturn(sprint);

        // Act
        Result<Sprint> result = sprintController.getById(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("Sprint 1", result.getData().getName());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(sprintMapper.findById(999L)).thenReturn(null);

        // Act
        Result<Sprint> result = sprintController.getById(999L);

        // Assert
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    // ==================== Update Sprint Tests ====================

    @Test
    void update_shouldReturnUpdatedSprint() {
        // Arrange
        Sprint sprint = createSprint(1L, "Updated Sprint", "PRJ_001");
        when(sprintService.update(any(Sprint.class))).thenReturn(sprint);

        // Act
        Result<Sprint> result = sprintController.update(1L, sprint);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1L, sprint.getId());
        verify(sprintService).update(sprint);
    }

    // ==================== Delete Sprint Tests ====================

    @Test
    void delete_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = sprintController.delete(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(sprintService).delete(1L);
    }

    // ==================== Start Sprint Tests ====================

    @Test
    void startSprint_shouldReturnStartedSprint() {
        // Arrange
        Sprint sprint = createSprint(1L, "Sprint 1", "PRJ_001");
        sprint.setStatus("ACTIVE");
        when(sprintService.startSprint(1L)).thenReturn(sprint);

        // Act
        Result<Sprint> result = sprintController.startSprint(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("ACTIVE", result.getData().getStatus());
    }

    // ==================== Complete Sprint Tests ====================

    @Test
    void completeSprint_shouldReturnCompletedSprint() {
        // Arrange
        Sprint sprint = createSprint(1L, "Sprint 1", "PRJ_001");
        sprint.setStatus("COMPLETED");
        when(sprintService.completeSprint(1L)).thenReturn(sprint);

        // Act
        Result<Sprint> result = sprintController.completeSprint(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals("COMPLETED", result.getData().getStatus());
    }

    // ==================== Get Sprint Tasks Tests ====================

    @Test
    void getSprintTasks_shouldReturnTaskList() {
        // Arrange
        List<Task> tasks = Arrays.asList(
            createTask(1L, "Task 1"),
            createTask(2L, "Task 2")
        );
        when(taskMapper.findBySprintId(1L)).thenReturn(tasks);

        // Act
        Result<List<Task>> result = sprintController.getSprintTasks(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    // ==================== Get Sprint Stats Tests ====================

    @Test
    void getSprintStats_shouldReturnStats() {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", 10);
        stats.put("completedTasks", 5);
        stats.put("remainingTasks", 5);
        stats.put("velocity", 40);
        when(sprintService.getSprintStats(1L)).thenReturn(stats);

        // Act
        Result<Map<String, Object>> result = sprintController.getSprintStats(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(10, result.getData().get("totalTasks"));
    }

    // ==================== Calculate Velocity Tests ====================

    @Test
    void calculateVelocity_shouldReturnVelocity() {
        // Arrange
        when(sprintService.calculateVelocity(1L)).thenReturn(42);

        // Act
        Result<Integer> result = sprintController.calculateVelocity(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(42, result.getData());
    }

    // ==================== Get Backlog Tasks Tests ====================

    @Test
    void getBacklogTasks_shouldReturnTaskList() {
        // Arrange
        List<Task> tasks = Collections.singletonList(createTask(1L, "Backlog Task"));
        when(sprintService.getBacklogTasks("PRJ_001")).thenReturn(tasks);

        // Act
        Result<List<Task>> result = sprintController.getBacklogTasks("PRJ_001");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    // ==================== Add Task to Sprint Tests ====================

    @Test
    void addTaskToSprint_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = sprintController.addTaskToSprint(1L, 1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(sprintService).addTaskToSprint(1L, 1L);
    }

    // ==================== Remove Task from Sprint Tests ====================

    @Test
    void removeTaskFromSprint_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = sprintController.removeTaskFromSprint(1L, 1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(sprintService).removeTaskFromSprint(1L);
    }

    // ==================== Batch Add Tasks Tests ====================

    @Test
    void batchAddTasks_shouldReturnMovedCount() {
        // Arrange
        Map<String, List<Long>> body = new HashMap<>();
        body.put("taskIds", Arrays.asList(1L, 2L, 3L));
        when(sprintService.batchAddTasks(1L, body.get("taskIds"))).thenReturn(3);

        // Act
        Result<Map<String, Integer>> result = sprintController.batchAddTasks(1L, body);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().get("movedCount"));
    }

    // ==================== Batch Remove Tasks Tests ====================

    @Test
    void batchRemoveTasks_shouldReturnMovedCount() {
        // Arrange
        Map<String, List<Long>> body = new HashMap<>();
        body.put("taskIds", Arrays.asList(1L, 2L));
        when(sprintService.batchRemoveTasks(eq(1L), any())).thenReturn(2);

        // Act
        Result<Map<String, Integer>> result = sprintController.batchRemoveTasks(1L, body);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().get("movedCount"));
    }

    // ==================== Helper Methods ====================

    private Sprint createSprint(Long id, String name, String projectId) {
        Sprint sprint = new Sprint();
        sprint.setId(id);
        sprint.setName(name);
        sprint.setProjectId(projectId);
        sprint.setStatus("PLANNING");
        sprint.setStartDate(LocalDate.now());
        sprint.setEndDate(LocalDate.now().plusDays(14));
        return sprint;
    }

    private Task createTask(Long id, String title) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setType("TASK");
        return task;
    }
}
