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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectStatsServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private ProjectStatsServiceImpl projectStatsService;

    private Project testProject;
    private Sprint testSprint;
    private Task completedTask;
    private Task inProgressTask;
    private Task notStartedTask;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStatus("ACTIVE");

        testSprint = new Sprint();
        testSprint.setId(1L);
        testSprint.setProjectId("PRJ_001");
        testSprint.setName("Sprint 1");
        testSprint.setStartDate(LocalDate.now().minusDays(7));
        testSprint.setEndDate(LocalDate.now().plusDays(7));

        completedTask = new Task();
        completedTask.setId(1L);
        completedTask.setProjectId("PRJ_001");
        completedTask.setSprintId(1L);
        completedTask.setTitle("Completed Task");
        completedTask.setEstimateHours(8);
        completedTask.setProgress(100);
        completedTask.setCompletionDate(LocalDateTime.now().minusDays(1));

        inProgressTask = new Task();
        inProgressTask.setId(2L);
        inProgressTask.setProjectId("PRJ_001");
        inProgressTask.setSprintId(1L);
        inProgressTask.setTitle("In Progress Task");
        inProgressTask.setEstimateHours(4);
        inProgressTask.setProgress(50);

        notStartedTask = new Task();
        notStartedTask.setId(3L);
        notStartedTask.setProjectId("PRJ_001");
        notStartedTask.setSprintId(1L);
        notStartedTask.setTitle("Not Started Task");
        notStartedTask.setEstimateHours(2);
        notStartedTask.setProgress(0);
    }

    @Test
    void testGetProjectStats_shouldReturnCompletionRate() {
        // Arrange
        List<Task> tasks = Arrays.asList(completedTask, inProgressTask, notStartedTask);
        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats(1L);

        // Assert
        assertEquals(1L, stats.get("projectId"));
        assertEquals("Test Project", stats.get("projectName"));
        assertEquals(3, stats.get("totalTasks"));
        assertEquals(1, stats.get("completedTasks"));
        assertEquals(1, stats.get("inProgressTasks"));
        // Completion rate = 1/3 * 100 = 33.33
        assertEquals(33.33, stats.get("completionRate"));
        // Work efficiency = 8/14 * 100 = 57.14
        assertEquals(57.14, stats.get("workEfficiency"));
        assertEquals(14, stats.get("totalEstimateHours"));
        assertEquals(8, stats.get("completedHours"));
    }

    @Test
    void testGetProjectStats_shouldReturnEmptyStats_whenProjectNotFound() {
        // Arrange
        when(projectMapper.findById(999L)).thenReturn(null);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats(999L);

        // Assert
        assertTrue(stats.isEmpty());
    }

    @Test
    void testGetProjectStats_shouldHandleNullEstimateHours() {
        // Arrange
        Task taskWithNullEstimate = new Task();
        taskWithNullEstimate.setId(4L);
        taskWithNullEstimate.setProjectId("PRJ_001");
        taskWithNullEstimate.setEstimateHours(null);
        taskWithNullEstimate.setProgress(100);

        List<Task> tasks = Collections.singletonList(taskWithNullEstimate);
        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats(1L);

        // Assert
        assertEquals(1, stats.get("totalTasks"));
        assertEquals(0, stats.get("totalEstimateHours"));
        assertEquals(0, stats.get("completedHours"));
    }

    @Test
    void testCompareProjects_shouldReturnMultipleProjects() {
        // Arrange
        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Project 2");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setProjectId("PRJ_001");
        task1.setEstimateHours(10);
        task1.setProgress(100);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProjectId("PRJ_002");
        task2.setEstimateHours(20);
        task2.setProgress(50);

        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(projectMapper.findById(2L)).thenReturn(project2);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Collections.singletonList(task1));
        when(taskMapper.findByProjectId("PRJ_002")).thenReturn(Collections.singletonList(task2));

        // Act
        List<Map<String, Object>> comparisons = projectStatsService.compareProjects(Arrays.asList(1L, 2L));

        // Assert
        assertEquals(2, comparisons.size());
        assertEquals("Test Project", comparisons.get(0).get("projectName"));
        assertEquals("Project 2", comparisons.get(1).get("projectName"));
    }

    @Test
    void testCompareProjects_shouldHandleEmptyList() {
        // Act
        List<Map<String, Object>> comparisons = projectStatsService.compareProjects(Collections.emptyList());

        // Assert
        assertTrue(comparisons.isEmpty());
    }

    @Test
    void testGetBurndownData_shouldReturnIdealAndActual() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setEstimateHours(10);
        task1.setCompletionDate(LocalDateTime.now().minusDays(3));

        Task task2 = new Task();
        task2.setId(2L);
        task2.setEstimateHours(5);
        task2.setCompletionDate(LocalDateTime.now().minusDays(1));

        List<Task> tasks = Arrays.asList(task1, task2);
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(taskMapper.findBySprintId(1L)).thenReturn(tasks);

        // Act
        Map<String, Object> burndown = projectStatsService.getBurndownData(1L);

        // Assert
        assertEquals(1L, burndown.get("sprintId"));
        assertEquals("Sprint 1", burndown.get("sprintName"));
        assertEquals(15, burndown.get("totalHours"));
        assertEquals(2, burndown.get("totalTasks"));
        assertNotNull(burndown.get("idealBurndown"));
        assertNotNull(burndown.get("actualBurndown"));
        assertTrue(burndown.get("idealBurndown") instanceof List);
        assertTrue(burndown.get("actualBurndown") instanceof List);
    }

    @Test
    void testGetBurndownData_shouldReturnEmptyMap_whenSprintNotFound() {
        // Arrange
        when(sprintMapper.findById(999L)).thenReturn(null);

        // Act
        Map<String, Object> burndown = projectStatsService.getBurndownData(999L);

        // Assert
        assertTrue(burndown.isEmpty());
    }

    @Test
    void testGetBurndownData_shouldHandleNullStartDate() {
        // Arrange
        Sprint sprintWithNullDates = new Sprint();
        sprintWithNullDates.setId(2L);
        sprintWithNullDates.setProjectId("PRJ_001");
        sprintWithNullDates.setName("Sprint 2");
        sprintWithNullDates.setStartDate(null);
        sprintWithNullDates.setEndDate(null);

        when(sprintMapper.findById(2L)).thenReturn(sprintWithNullDates);
        when(taskMapper.findBySprintId(2L)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> burndown = projectStatsService.getBurndownData(2L);

        // Assert
        assertEquals(2L, burndown.get("sprintId"));
        assertEquals(0, burndown.get("totalHours"));
        assertEquals(0, burndown.get("totalTasks"));
        // Default 14 days should be used
        assertEquals(14L, burndown.get("totalDays"));
    }

    @Test
    void testGetBurndownData_shouldHandleTasksWithNullCompletionDate() {
        // Arrange
        Task taskNoCompletion = new Task();
        taskNoCompletion.setId(1L);
        taskNoCompletion.setEstimateHours(10);
        taskNoCompletion.setCompletionDate(null);

        List<Task> tasks = Collections.singletonList(taskNoCompletion);
        when(sprintMapper.findById(1L)).thenReturn(testSprint);
        when(taskMapper.findBySprintId(1L)).thenReturn(tasks);

        // Act
        Map<String, Object> burndown = projectStatsService.getBurndownData(1L);

        // Assert
        assertEquals(10, burndown.get("totalHours"));
        assertEquals(1, burndown.get("totalTasks"));
        // Tasks without completion date don't contribute to actual burndown
    }

    @Test
    void testGetProjectStats_shouldHandleNullProgress() {
        // Arrange
        Task taskWithNullProgress = new Task();
        taskWithNullProgress.setId(1L);
        taskWithNullProgress.setProjectId("PRJ_001");
        taskWithNullProgress.setEstimateHours(10);
        taskWithNullProgress.setProgress(null);

        List<Task> tasks = Collections.singletonList(taskWithNullProgress);
        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats(1L);

        // Assert
        assertEquals(1, stats.get("totalTasks"));
        assertEquals(0, stats.get("completedTasks"));
        assertEquals(0, stats.get("inProgressTasks"));
        assertEquals(0.0, stats.get("completionRate"));
    }

    @Test
    void testGetProjectStats_shouldHandleAllTasksCompleted() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setProjectId("PRJ_001");
        task1.setEstimateHours(5);
        task1.setProgress(100);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProjectId("PRJ_001");
        task2.setEstimateHours(10);
        task2.setProgress(100);

        List<Task> tasks = Arrays.asList(task1, task2);
        when(projectMapper.findById(1L)).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats(1L);

        // Assert
        assertEquals(2, stats.get("totalTasks"));
        assertEquals(2, stats.get("completedTasks"));
        assertEquals(0, stats.get("inProgressTasks"));
        assertEquals(100.0, stats.get("completionRate"));
        assertEquals(100.0, stats.get("workEfficiency"));
    }
}
