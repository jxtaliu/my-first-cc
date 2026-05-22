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
        testSprint.setProjectId(1L);
        testSprint.setName("Sprint 1");
        testSprint.setStatus(1);
        testSprint.setStartDate(LocalDateTime.now());
        testSprint.setEndDate(LocalDateTime.now().plusDays(14));

        testTask1 = new Task();
        testTask1.setId(1L);
        testTask1.setProjectId(1L);
        testTask1.setSprintId(1L);
        testTask1.setTitle("Task 1");
        testTask1.setEstimateHours(8);
        testTask1.setProgress(100);

        testTask2 = new Task();
        testTask2.setId(2L);
        testTask2.setProjectId(1L);
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
    void testCalculateCapacity_shouldCalculateBasedOnTeamSize() {
        // Arrange
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(projectMapper.findMemberIds(1L)).thenReturn(Arrays.asList(1L, 2L, 3L));

        // Act
        int capacity = sprintService.calculateCapacity(1L);

        // Assert
        // teamSize=3, durationDays=14, workingHoursPerDay=8
        // capacity = 3 * 14 * 8 = 336
        assertEquals(336, capacity);
    }

    @Test
    void testGetSprintStats_shouldReturnCorrectStats() {
        // Arrange
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        when(taskMapper.findBySprintId(1L)).thenReturn(tasks);
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(projectMapper.findMemberIds(1L)).thenReturn(Arrays.asList(1L, 2L));

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
        backlogTask.setProjectId(1L);
        backlogTask.setSprintId(null);

        when(taskMapper.selectList(any())).thenReturn(Collections.singletonList(backlogTask));

        // Act
        List<Task> backlogTasks = sprintService.getBacklogTasks(1L);

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
    void testCalculateCapacity_shouldReturnZero_whenProjectNotFound() {
        // Arrange
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(projectMapper.findById(1L)).thenReturn(null);

        // Act
        int capacity = sprintService.calculateCapacity(1L);

        // Assert
        assertEquals(0, capacity);
    }

    @Test
    void testGetSprintStats_shouldHandleNullProgress() {
        // Arrange
        Task taskWithNullProgress = new Task();
        taskWithNullProgress.setId(3L);
        taskWithNullProgress.setProjectId(1L);
        taskWithNullProgress.setSprintId(1L);
        taskWithNullProgress.setProgress(null);
        taskWithNullProgress.setEstimateHours(10);

        when(taskMapper.findBySprintId(1L)).thenReturn(Collections.singletonList(taskWithNullProgress));
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(projectMapper.findMemberIds(1L)).thenReturn(Collections.singletonList(1L));

        // Act
        Map<String, Object> stats = sprintService.getSprintStats(1L);

        // Assert
        assertEquals(1, stats.get("totalTasks"));
        assertEquals(0, stats.get("completedTasks")); // Null progress is not completed
        assertEquals(1, stats.get("remainingTasks"));
    }
}
