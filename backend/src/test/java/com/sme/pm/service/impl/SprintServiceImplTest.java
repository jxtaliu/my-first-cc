package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintServiceImplTest {

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private SprintServiceImpl sprintService;

    private Sprint testSprint;
    private Task testTask1;
    private Task testTask2;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testSprint = new Sprint();
        testSprint.setId(1L);
        testSprint.setProjectId("PRJ_001");
        testSprint.setName("Sprint 1");
        testSprint.setStatus("PLANNING");
        testSprint.setStartDate(LocalDate.now());
        testSprint.setEndDate(LocalDate.now().plusDays(14));
        testSprint.setCapacityHours(80);

        testTask1 = new Task();
        testTask1.setId(1L);
        testTask1.setProjectId("PRJ_001");
        testTask1.setSprintId(1L);
        testTask1.setTitle("Task 1");
        testTask1.setEstimateHours(8);
        testTask1.setProgress(100);

        testTask2 = new Task();
        testTask2.setId(2L);
        testTask2.setProjectId("PRJ_001");
        testTask2.setSprintId(1L);
        testTask2.setTitle("Task 2");
        testTask2.setEstimateHours(4);
        testTask2.setProgress(50);

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
    }

    @Test
    void testCalculateVelocity_shouldSumCompletedTaskHours() {
        // Arrange
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        when(taskMapper.findBySprintId(1L)).thenReturn(tasks);

        // Act
        int velocity = sprintService.calculateVelocity(1L);

        // Assert
        // Only completed task (progress=100) contributes to velocity
        assertEquals(8, velocity);
        verify(taskMapper).findBySprintId(1L);
    }

    @Test
    void testCalculateCapacity_shouldReturnSprintCapacityHours() {
        // Arrange
        when(sprintMapper.findById(1L)).thenReturn(testSprint);

        // Act
        int capacity = sprintService.calculateCapacity(1L);

        // Assert - returns sprint's capacityHours field directly
        assertEquals(80, capacity);
    }

    @Test
    void testGetSprintStats_shouldReturnCorrectStats() {
        // Arrange
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        when(taskMapper.findBySprintId(1L)).thenReturn(tasks);
        when(sprintMapper.findById(1L)).thenReturn(testSprint);

        // Act
        Map<String, Object> stats = sprintService.getSprintStats(1L);

        // Assert
        assertEquals(2, stats.get("totalTasks"));
        assertEquals(1, stats.get("completedTasks")); // Only Task1 is completed
        assertEquals(1, stats.get("remainingTasks")); // Task2 is not completed
        assertEquals(8, stats.get("velocity")); // Only completed task hours
    }

    @Test
    void testAddTaskToSprint_shouldUpdateTaskSprintId() {
        // Arrange
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(taskMapper.findById(1L)).thenReturn(testTask1);

        // Act
        sprintService.addTaskToSprint(1L, 1L);

        // Assert
        assertEquals(1L, testTask1.getSprintId());
        verify(taskMapper).updateById(testTask1);
    }

    @Test
    void testAddTaskToSprint_shouldThrowException_whenSprintNotFound() {
        // Arrange
        when(sprintMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            sprintService.addTaskToSprint(999L, 1L);
        });
    }

    @Test
    void testAddTaskToSprint_shouldThrowException_whenTaskNotFound() {
        // Arrange
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(taskMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            sprintService.addTaskToSprint(1L, 999L);
        });
    }

    @Test
    void testRemoveTaskFromSprint_shouldSetSprintIdToNull() {
        // Arrange
        when(taskMapper.findById(1L)).thenReturn(testTask1);

        // Act
        sprintService.removeTaskFromSprint(1L);

        // Assert
        assertNull(testTask1.getSprintId());
        verify(taskMapper).updateById(testTask1);
    }

    @Test
    void testRemoveTaskFromSprint_shouldThrowException_whenTaskNotFound() {
        // Arrange
        when(taskMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            sprintService.removeTaskFromSprint(999L);
        });
    }

    @Test
    void testGetBacklogTasks_shouldReturnTasksWithNoSprint() {
        // Arrange
        Task backlogTask = new Task();
        backlogTask.setId(3L);
        backlogTask.setProjectId("PRJ_001");
        backlogTask.setSprintId(null);

        when(taskMapper.selectList(any())).thenReturn(Collections.singletonList(backlogTask));

        // Act
        List<Task> backlogTasks = sprintService.getBacklogTasks("PRJ_001");

        // Assert
        assertEquals(1, backlogTasks.size());
        assertNull(backlogTasks.get(0).getSprintId());
    }

    @Test
    void testCalculateVelocity_shouldReturnZero_whenNoTasks() {
        // Arrange
        when(taskMapper.findBySprintId(1L)).thenReturn(Collections.emptyList());

        // Act
        int velocity = sprintService.calculateVelocity(1L);

        // Assert
        assertEquals(0, velocity);
    }

    @Test
    void testCalculateVelocity_shouldReturnZero_whenSprintNotFound() {
        // Arrange
        when(taskMapper.findBySprintId(999L)).thenReturn(Collections.emptyList());

        // Act
        int velocity = sprintService.calculateVelocity(999L);

        // Assert
        assertEquals(0, velocity);
    }

    @Test
    void testCalculateCapacity_shouldReturnZero_whenSprintNotFound() {
        // Arrange
        when(sprintMapper.findById(999L)).thenReturn(null);

        // Act
        int capacity = sprintService.calculateCapacity(999L);

        // Assert
        assertEquals(0, capacity);
    }

    @Test
    void testGetSprintStats_shouldHandleNullProgress() {
        // Arrange
        Task taskWithNullProgress = new Task();
        taskWithNullProgress.setId(3L);
        taskWithNullProgress.setProjectId("PRJ_001");
        taskWithNullProgress.setSprintId(1L);
        taskWithNullProgress.setProgress(null);
        taskWithNullProgress.setEstimateHours(10);

        when(taskMapper.findBySprintId(1L)).thenReturn(Collections.singletonList(taskWithNullProgress));
        when(sprintMapper.findById(1L)).thenReturn(testSprint);

        // Act
        Map<String, Object> stats = sprintService.getSprintStats(1L);

        // Assert
        assertEquals(1, stats.get("totalTasks"));
        assertEquals(0, stats.get("completedTasks")); // Null progress is not completed
        assertEquals(1, stats.get("remainingTasks"));
    }

    // ==================== create Tests ====================

    @Test
    void create_shouldSetStatusToPlanning_andInsertSprint() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setProjectId("PRJ_001");
        sprint.setName("New Sprint");

        when(sprintMapper.insert(any(Sprint.class))).thenReturn(1);

        // Act
        Sprint result = sprintService.create(sprint);

        // Assert
        assertEquals("PLANNING", result.getStatus());
        verify(sprintMapper).insert(sprint);
    }

    @Test
    void create_shouldThrowException_whenProjectIdIsNull() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setProjectId(null);
        sprint.setName("New Sprint");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sprintService.create(sprint)
        );
        assertTrue(exception.getMessage().contains("projectId不能为空"));
    }

    @Test
    void create_shouldThrowException_whenProjectIdIsEmpty() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setProjectId("");
        sprint.setName("New Sprint");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sprintService.create(sprint)
        );
        assertTrue(exception.getMessage().contains("projectId不能为空"));
    }

    @Test
    void create_shouldThrowException_whenNameIsNull() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setProjectId("PRJ_001");
        sprint.setName(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sprintService.create(sprint)
        );
        assertTrue(exception.getMessage().contains("name不能为空"));
    }

    @Test
    void create_shouldThrowException_whenNameIsEmpty() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setProjectId("PRJ_001");
        sprint.setName("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sprintService.create(sprint)
        );
        assertTrue(exception.getMessage().contains("name不能为空"));
    }

    // ==================== update Tests ====================

    @Test
    void update_shouldCallUpdateById() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setId(1L);
        sprint.setName("Updated Sprint");

        when(sprintMapper.updateById(sprint)).thenReturn(1);

        // Act
        Sprint result = sprintService.update(sprint);

        // Assert
        assertNotNull(result);
        verify(sprintMapper).updateById(sprint);
    }

    // ==================== delete Tests ====================

    @Test
    void delete_shouldCallDeleteById() {
        // Arrange
        Long sprintId = 1L;

        // Act
        sprintService.delete(sprintId);

        // Assert
        verify(sprintMapper).deleteById(sprintId);
    }

    // ==================== startSprint Tests ====================

    @Test
    void startSprint_shouldSetStatusToActiveAndSetStartDate() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setId(1L);
        sprint.setProjectId("PRJ_001");
        sprint.setName("Sprint 1");
        sprint.setStatus("PLANNING");

        when(sprintMapper.findById(1L)).thenReturn(sprint);
        when(sprintMapper.updateById(sprint)).thenReturn(1);

        // Act
        Sprint result = sprintService.startSprint(1L);

        // Assert
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getStartDate());
        verify(sprintMapper).updateById(sprint);
    }

    @Test
    void startSprint_shouldThrowException_whenSprintNotFound() {
        // Arrange
        when(sprintMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sprintService.startSprint(999L)
        );
        assertTrue(exception.getMessage().contains("Sprint not found"));
    }

    // ==================== completeSprint Tests ====================

    @Test
    void completeSprint_shouldSetStatusToCompletedAndSetEndDate() {
        // Arrange
        Sprint sprint = new Sprint();
        sprint.setId(1L);
        sprint.setProjectId("PRJ_001");
        sprint.setName("Sprint 1");
        sprint.setStatus("ACTIVE");

        when(sprintMapper.findById(1L)).thenReturn(sprint);
        when(sprintMapper.updateById(sprint)).thenReturn(1);

        // Act
        Sprint result = sprintService.completeSprint(1L);

        // Assert
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getEndDate());
        verify(sprintMapper).updateById(sprint);
    }

    @Test
    void completeSprint_shouldThrowException_whenSprintNotFound() {
        // Arrange
        when(sprintMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sprintService.completeSprint(999L)
        );
        assertTrue(exception.getMessage().contains("Sprint not found"));
    }

    // ==================== findByProjectId Tests ====================

    @Test
    void findByProjectId_shouldReturnSprints() {
        // Arrange
        Sprint sprint1 = new Sprint();
        sprint1.setId(1L);
        sprint1.setProjectId("PRJ_001");

        Sprint sprint2 = new Sprint();
        sprint2.setId(2L);
        sprint2.setProjectId("PRJ_001");

        when(sprintMapper.findByProjectId("PRJ_001")).thenReturn(Arrays.asList(sprint1, sprint2));

        // Act
        List<Sprint> result = sprintService.findByProjectId("PRJ_001");

        // Assert
        assertEquals(2, result.size());
        verify(sprintMapper).findByProjectId("PRJ_001");
    }

    @Test
    void findByProjectId_shouldReturnEmptyList_whenNoSprints() {
        // Arrange
        when(sprintMapper.findByProjectId("PRJ_999")).thenReturn(Collections.emptyList());

        // Act
        List<Sprint> result = sprintService.findByProjectId("PRJ_999");

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== findById Tests ====================

    @Test
    void findById_shouldReturnSprint() {
        // Arrange
        when(sprintMapper.findById(1L)).thenReturn(testSprint);

        // Act
        Sprint result = sprintService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(sprintMapper).findById(1L);
    }

    @Test
    void findById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(sprintMapper.findById(999L)).thenReturn(null);

        // Act
        Sprint result = sprintService.findById(999L);

        // Assert
        assertNull(result);
    }

    // ==================== batchAddTasks Tests ====================

    @Test
    void batchAddTasks_shouldReturnZero_whenTaskIdsIsNull() {
        // Act
        int count = sprintService.batchAddTasks(1L, null);

        // Assert
        assertEquals(0, count);
        verifyNoMoreInteractions(taskMapper);
    }

    @Test
    void batchAddTasks_shouldReturnZero_whenTaskIdsIsEmpty() {
        // Act
        int count = sprintService.batchAddTasks(1L, Collections.emptyList());

        // Assert
        assertEquals(0, count);
        verifyNoMoreInteractions(taskMapper);
    }

    @Test
    void batchAddTasks_shouldAddTasksToSprint() {
        // Arrange
        Task task1 = new Task();
        task1.setId(10L);
        task1.setProjectId("PRJ_001");

        Task task2 = new Task();
        task2.setId(11L);
        task2.setProjectId("PRJ_001");

        when(taskMapper.selectById(10L)).thenReturn(task1);
        when(taskMapper.selectById(11L)).thenReturn(task2);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        // Act
        int count = sprintService.batchAddTasks(1L, Arrays.asList(10L, 11L));

        // Assert
        assertEquals(2, count);
        assertEquals(1L, task1.getSprintId());
        assertEquals(1L, task2.getSprintId());
    }

    @Test
    void batchAddTasks_shouldSkipNullTasks() {
        // Arrange
        when(taskMapper.selectById(10L)).thenReturn(null);

        // Act
        int count = sprintService.batchAddTasks(1L, Collections.singletonList(10L));

        // Assert
        assertEquals(0, count);
    }

    // ==================== batchRemoveTasks Tests ====================

    // Note: batchRemoveTasks uses LambdaUpdateWrapper which requires MyBatis-Plus entity metadata
    // and cannot be properly mocked in unit tests. This is a known limitation.
    // The method is tested for null/empty input handling only.

    @Test
    void batchRemoveTasks_shouldReturnZero_whenTaskIdsIsNull() {
        // Act
        int count = sprintService.batchRemoveTasks(1L, null);

        // Assert
        assertEquals(0, count);
    }

    @Test
    void batchRemoveTasks_shouldReturnZero_whenTaskIdsIsEmpty() {
        // Act
        int count = sprintService.batchRemoveTasks(1L, Collections.emptyList());

        // Assert
        assertEquals(0, count);
    }

    // ==================== calculateVelocity Edge Cases ====================

    @Test
    void calculateVelocity_shouldOnlyCountCompletedTasks() {
        // Arrange
        Task completedTask = new Task();
        completedTask.setId(1L);
        completedTask.setProgress(100);
        completedTask.setEstimateHours(10);

        Task incompleteTask = new Task();
        incompleteTask.setId(2L);
        incompleteTask.setProgress(50);
        incompleteTask.setEstimateHours(10);

        Task zeroProgressTask = new Task();
        zeroProgressTask.setId(3L);
        zeroProgressTask.setProgress(0);
        zeroProgressTask.setEstimateHours(10);

        when(taskMapper.findBySprintId(1L))
            .thenReturn(Arrays.asList(completedTask, incompleteTask, zeroProgressTask));

        // Act
        int velocity = sprintService.calculateVelocity(1L);

        // Assert
        assertEquals(10, velocity); // Only completed task counts
    }

    @Test
    void calculateVelocity_shouldHandleNullEstimateHours() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setProgress(100);
        task.setEstimateHours(null);

        when(taskMapper.findBySprintId(1L)).thenReturn(Collections.singletonList(task));

        // Act
        int velocity = sprintService.calculateVelocity(1L);

        // Assert
        assertEquals(0, velocity); // Null estimate contributes 0
    }

    // ==================== addTaskToSprint with Milestone ====================

    @Test
    void addTaskToSprint_shouldAutoLinkMilestone_whenSprintHasMilestone() {
        // Arrange
        Sprint sprintWithMilestone = new Sprint();
        sprintWithMilestone.setId(1L);
        sprintWithMilestone.setProjectId("PRJ_001");
        sprintWithMilestone.setMilestoneId(100L);

        Task taskWithoutMilestone = new Task();
        taskWithoutMilestone.setId(1L);
        taskWithoutMilestone.setProjectId("PRJ_001");
        taskWithoutMilestone.setMilestoneId(null);

        when(sprintMapper.findById(1L)).thenReturn(sprintWithMilestone);
        when(taskMapper.findById(1L)).thenReturn(taskWithoutMilestone);

        // Act
        sprintService.addTaskToSprint(1L, 1L);

        // Assert
        assertEquals(100L, taskWithoutMilestone.getMilestoneId());
        verify(taskMapper).updateById(taskWithoutMilestone);
    }

    @Test
    void addTaskToSprint_shouldNotOverwriteTaskMilestone() {
        // Arrange
        Sprint sprintWithMilestone = new Sprint();
        sprintWithMilestone.setId(1L);
        sprintWithMilestone.setProjectId("PRJ_001");
        sprintWithMilestone.setMilestoneId(100L);

        Task taskWithMilestone = new Task();
        taskWithMilestone.setId(1L);
        taskWithMilestone.setProjectId("PRJ_001");
        taskWithMilestone.setMilestoneId(200L); // Already has milestone

        when(sprintMapper.findById(1L)).thenReturn(sprintWithMilestone);
        when(taskMapper.findById(1L)).thenReturn(taskWithMilestone);

        // Act
        sprintService.addTaskToSprint(1L, 1L);

        // Assert
        assertEquals(200L, taskWithMilestone.getMilestoneId()); // Should remain unchanged
        verify(taskMapper).updateById(taskWithMilestone);
    }

    // ==================== getSprintStats Edge Cases ====================

    @Test
    void getSprintStats_shouldCalculateCorrectVelocityAndCapacity() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setProgress(100);
        task1.setEstimateHours(8);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProgress(100);
        task2.setEstimateHours(4);

        Sprint sprint = new Sprint();
        sprint.setId(1L);
        sprint.setCapacityHours(80);

        when(taskMapper.findBySprintId(1L)).thenReturn(Arrays.asList(task1, task2));
        when(sprintMapper.findById(1L)).thenReturn(sprint);

        // Act
        Map<String, Object> stats = sprintService.getSprintStats(1L);

        // Assert
        assertEquals(2, stats.get("totalTasks"));
        assertEquals(2, stats.get("completedTasks"));
        assertEquals(0, stats.get("remainingTasks"));
        assertEquals(12, stats.get("velocity")); // 8 + 4
        assertEquals(80, stats.get("capacity"));
    }
}
