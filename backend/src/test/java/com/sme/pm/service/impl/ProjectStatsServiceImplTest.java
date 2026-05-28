package com.sme.pm.service.impl;

import com.sme.pm.entity.Milestone;
import com.sme.pm.entity.Project;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.MilestoneMapper;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectStatsServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private MilestoneMapper milestoneMapper;

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
        testProject.setProjectId("PRJ_001");
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
        completedTask.setType("TASK"); // Must be TASK or SUBTASK to be counted
        completedTask.setSprintId(1L);
        completedTask.setTitle("Completed Task");
        completedTask.setEstimateHours(8);
        completedTask.setProgress(100);
        completedTask.setCompletionDate(LocalDateTime.now().minusDays(1));

        inProgressTask = new Task();
        inProgressTask.setId(2L);
        inProgressTask.setProjectId("PRJ_001");
        inProgressTask.setType("TASK"); // Must be TASK or SUBTASK to be counted
        inProgressTask.setSprintId(1L);
        inProgressTask.setTitle("In Progress Task");
        inProgressTask.setEstimateHours(4);
        inProgressTask.setProgress(50);

        notStartedTask = new Task();
        notStartedTask.setId(3L);
        notStartedTask.setProjectId("PRJ_001");
        notStartedTask.setType("TASK"); // Must be TASK or SUBTASK to be counted
        notStartedTask.setSprintId(1L);
        notStartedTask.setTitle("Not Started Task");
        notStartedTask.setEstimateHours(2);
        notStartedTask.setProgress(0);
    }

    @Test
    void testGetProjectStats_shouldReturnCompletionRate() {
        // Arrange
        List<Task> tasks = Arrays.asList(completedTask, inProgressTask, notStartedTask);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

        // Assert
        assertEquals("PRJ_001", stats.get("projectId"));
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
        when(projectMapper.findByProjectId("NOT_EXIST")).thenReturn(null);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("NOT_EXIST");

        // Assert
        assertTrue(stats.isEmpty());
    }

    @Test
    void testGetProjectStats_shouldHandleNullEstimateHours() {
        // Arrange
        Task taskWithNullEstimate = new Task();
        taskWithNullEstimate.setId(4L);
        taskWithNullEstimate.setProjectId("PRJ_001");
        taskWithNullEstimate.setType("TASK"); // Must be TASK or SUBTASK to be counted
        taskWithNullEstimate.setEstimateHours(null);
        taskWithNullEstimate.setProgress(100);

        List<Task> tasks = Collections.singletonList(taskWithNullEstimate);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

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

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(projectMapper.findByProjectId("PRJ_002")).thenReturn(project2);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Collections.singletonList(task1));
        when(taskMapper.findByProjectId("PRJ_002")).thenReturn(Collections.singletonList(task2));

        // Act
        List<Map<String, Object>> comparisons = projectStatsService.compareProjects(Arrays.asList("PRJ_001", "PRJ_002"));

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
        taskWithNullProgress.setType("TASK"); // Must be TASK or SUBTASK to be counted
        taskWithNullProgress.setEstimateHours(10);
        taskWithNullProgress.setProgress(null);

        List<Task> tasks = Collections.singletonList(taskWithNullProgress);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

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
        task1.setType("TASK"); // Must be TASK or SUBTASK to be counted
        task1.setEstimateHours(5);
        task1.setProgress(100);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProjectId("PRJ_001");
        task2.setType("TASK"); // Must be TASK or SUBTASK to be counted
        task2.setEstimateHours(10);
        task2.setProgress(100);

        List<Task> tasks = Arrays.asList(task1, task2);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

        // Assert
        assertEquals(2, stats.get("totalTasks"));
        assertEquals(2, stats.get("completedTasks"));
        assertEquals(0, stats.get("inProgressTasks"));
        assertEquals(100.0, stats.get("completionRate"));
        assertEquals(100.0, stats.get("workEfficiency"));
    }

    @Test
    void testGetProjectStats_shouldCountBlockedTasks() {
        // Arrange
        Task blockedTask = new Task();
        blockedTask.setId(1L);
        blockedTask.setProjectId("PRJ_001");
        blockedTask.setType("TASK");
        blockedTask.setStatus("BLOCKED");
        blockedTask.setProgress(50);

        List<Task> tasks = Collections.singletonList(blockedTask);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

        // Assert
        assertEquals(1, stats.get("blockedTasks"));
    }

    @Test
    void testGetProjectStats_shouldCountOverdueTasks() {
        // Arrange
        Task overdueTask = new Task();
        overdueTask.setId(1L);
        overdueTask.setProjectId("PRJ_001");
        overdueTask.setType("TASK");
        overdueTask.setStatus("IN_PROGRESS");
        overdueTask.setProgress(50);
        overdueTask.setDueDate(LocalDate.now().minusDays(1)); // Overdue

        List<Task> tasks = Collections.singletonList(overdueTask);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

        // Assert
        assertEquals(1, stats.get("overdueTasks"));
    }

    @Test
    void testGetProjectStats_shouldNotCountEpicFeatureStory() {
        // Arrange
        Task epicTask = new Task();
        epicTask.setId(1L);
        epicTask.setProjectId("PRJ_001");
        epicTask.setType("EPIC");
        epicTask.setProgress(100);

        Task featureTask = new Task();
        featureTask.setId(2L);
        featureTask.setProjectId("PRJ_001");
        featureTask.setType("FEATURE");
        featureTask.setProgress(100);

        Task storyTask = new Task();
        storyTask.setId(3L);
        storyTask.setProjectId("PRJ_001");
        storyTask.setType("STORY");
        storyTask.setProgress(100);

        Task regularTask = new Task();
        regularTask.setId(4L);
        regularTask.setProjectId("PRJ_001");
        regularTask.setType("TASK");
        regularTask.setProgress(100);

        List<Task> tasks = Arrays.asList(epicTask, featureTask, storyTask, regularTask);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

        // Assert - only TASK and SUBTASK are counted
        assertEquals(1, stats.get("totalTasks"));
        assertEquals(1, stats.get("completedTasks"));
    }

    @Test
    void testGetProjectStats_shouldIncludeTypeStats() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        task.setProgress(100);

        Task bug = new Task();
        bug.setId(2L);
        bug.setProjectId("PRJ_001");
        bug.setType("BUG");
        bug.setProgress(50);

        List<Task> tasks = Arrays.asList(task, bug);
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(tasks);

        // Act
        Map<String, Object> stats = projectStatsService.getProjectStats("PRJ_001");

        // Assert
        assertNotNull(stats.get("typeStats"));
        assertTrue(stats.get("typeStats") instanceof List);
    }

    @Test
    void testGetTeamThroughput_shouldReturnEmpty_whenNoCompletedTasks() {
        // Arrange
        Task uncompletedTask = new Task();
        uncompletedTask.setId(1L);
        uncompletedTask.setProjectId("PRJ_001");
        uncompletedTask.setCompletionDate(null); // Not completed

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Collections.singletonList(uncompletedTask));

        // Act
        Map<String, Object> throughput = projectStatsService.getTeamThroughput("PRJ_001", null, null);

        // Assert
        assertEquals(0, throughput.get("totalCompleted"));
        assertNotNull(throughput.get("weeklyThroughput"));
        assertNotNull(throughput.get("dailyThroughput"));
    }

    @Test
    void testGetTeamThroughput_shouldFilterByDateRange() {
        // Arrange
        // task1 created within range and completed within range
        Task task1 = new Task();
        task1.setId(1L);
        task1.setProjectId("PRJ_001");
        task1.setCompletionDate(LocalDateTime.now().minusDays(5));
        task1.setCreatedAt(LocalDateTime.now().minusDays(6)); // Within date range

        // task2 created outside range
        Task task2 = new Task();
        task2.setId(2L);
        task2.setProjectId("PRJ_001");
        task2.setCompletionDate(LocalDateTime.now().minusDays(20));
        task2.setCreatedAt(LocalDateTime.now().minusDays(25)); // Outside range

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Arrays.asList(task1, task2));

        // Act
        String startDate = LocalDate.now().minusDays(7).toString();
        String endDate = LocalDate.now().toString();
        Map<String, Object> throughput = projectStatsService.getTeamThroughput("PRJ_001", startDate, endDate);

        // Assert - only task1 should be counted (created within range and completed)
        assertEquals(1, throughput.get("totalCompleted"));
        assertEquals(startDate, throughput.get("startDate"));
        assertEquals(endDate, throughput.get("endDate"));
    }

    @Test
    void testGetTeamThroughput_shouldReturnEmpty_whenProjectNotFound() {
        // Arrange
        when(projectMapper.findByProjectId("NOT_EXIST")).thenReturn(null);

        // Act
        Map<String, Object> throughput = projectStatsService.getTeamThroughput("NOT_EXIST", null, null);

        // Assert
        assertTrue(throughput.isEmpty());
    }

    @Test
    void testGetCfdData_shouldReturnEmpty_whenProjectNotFound() {
        // Arrange
        when(projectMapper.findByProjectId("NOT_EXIST")).thenReturn(null);

        // Act
        List<Map<String, Object>> cfdData = projectStatsService.getCfdData("NOT_EXIST");

        // Assert
        assertTrue(cfdData.isEmpty());
    }

    @Test
    void testGetCfdData_shouldReturnEmpty_whenNoTasks() {
        // Arrange
        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Collections.emptyList());

        // Act
        List<Map<String, Object>> cfdData = projectStatsService.getCfdData("PRJ_001");

        // Assert
        assertTrue(cfdData.isEmpty());
    }

    @Test
    void testGetCfdData_shouldGenerateDataForTasks() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setProjectId("PRJ_001");
        task1.setType("TASK");
        task1.setStatus("IN_PROGRESS");
        task1.setCreatedAt(LocalDateTime.now().minusDays(5));

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProjectId("PRJ_001");
        task2.setType("TASK");
        task2.setStatus("DONE");
        task2.setCreatedAt(LocalDateTime.now().minusDays(10));
        task2.setCompletionDate(LocalDateTime.now().minusDays(3));

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Arrays.asList(task1, task2));

        // Act
        List<Map<String, Object>> cfdData = projectStatsService.getCfdData("PRJ_001");

        // Assert
        assertFalse(cfdData.isEmpty());
        Map<String, Object> firstDay = cfdData.get(0);
        assertNotNull(firstDay.get("date"));
        assertNotNull(firstDay.get("todo"));
        assertNotNull(firstDay.get("in_progress"));
        assertNotNull(firstDay.get("done"));
    }

    @Test
    void testGetHeatmapData_shouldReturnEmpty_whenProjectNotFound() {
        // Arrange
        when(projectMapper.findByProjectId("NOT_EXIST")).thenReturn(null);

        // Act
        List<Map<String, Object>> heatmapData = projectStatsService.getHeatmapData("NOT_EXIST");

        // Assert
        assertTrue(heatmapData.isEmpty());
    }

    @Test
    void testGetHeatmapData_shouldGroupByAssigneeAndPriority() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setProjectId("PRJ_001");
        task1.setType("TASK");
        task1.setAssigneeName("John");
        task1.setPriority("P1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProjectId("PRJ_001");
        task2.setType("TASK");
        task2.setAssigneeName("John");
        task2.setPriority("P1");

        Task task3 = new Task();
        task3.setId(3L);
        task3.setProjectId("PRJ_001");
        task3.setType("TASK");
        task3.setAssigneeName("Jane");
        task3.setPriority("P2");

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Arrays.asList(task1, task2, task3));

        // Act
        List<Map<String, Object>> heatmapData = projectStatsService.getHeatmapData("PRJ_001");

        // Assert
        assertEquals(2, heatmapData.size()); // John+P1 (count=2), Jane+P2 (count=1)
    }

    @Test
    void testGetHeatmapData_shouldHandleNullAssigneeAndPriority() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        task.setAssigneeName(null);
        task.setPriority(null);

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Collections.singletonList(task));

        // Act
        List<Map<String, Object>> heatmapData = projectStatsService.getHeatmapData("PRJ_001");

        // Assert
        assertEquals(1, heatmapData.size());
        assertEquals("Unassigned", heatmapData.get(0).get("assignee"));
        assertEquals("None", heatmapData.get(0).get("priority"));
    }

    @Test
    void testGetMilestoneProgress_shouldReturnEmpty_whenProjectNotFound() {
        // Arrange
        when(projectMapper.findByProjectId("NOT_EXIST")).thenReturn(null);

        // Act
        List<Map<String, Object>> milestoneData = projectStatsService.getMilestoneProgress("NOT_EXIST");

        // Assert
        assertTrue(milestoneData.isEmpty());
    }

    @Test
    void testGetMilestoneProgress_shouldCalculateProgress() {
        // Arrange
        Milestone milestone = new Milestone();
        milestone.setId(1L);
        milestone.setName("M1");
        milestone.setStatus("ACTIVE");
        milestone.setTargetDate(LocalDate.now().plusDays(30));

        Task task1 = new Task();
        task1.setId(1L);
        task1.setMilestoneId(1L);
        task1.setStatus("DONE");
        task1.setProgress(100);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setMilestoneId(1L);
        task2.setStatus("IN_PROGRESS");
        task2.setProgress(50);

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(milestoneMapper.findByProjectId("PRJ_001")).thenReturn(Collections.singletonList(milestone));
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Arrays.asList(task1, task2));

        // Act
        List<Map<String, Object>> milestoneData = projectStatsService.getMilestoneProgress("PRJ_001");

        // Assert
        assertEquals(1, milestoneData.size());
        assertEquals(2, milestoneData.get(0).get("total"));
        assertEquals(1, milestoneData.get(0).get("completed"));
    }

    @Test
    void testGetMilestoneProgress_shouldHandleMilestoneWithNoTasks() {
        // Arrange
        Milestone milestone = new Milestone();
        milestone.setId(1L);
        milestone.setName("Empty Milestone");
        milestone.setStatus("ACTIVE");

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(milestoneMapper.findByProjectId("PRJ_001")).thenReturn(Collections.singletonList(milestone));
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Collections.emptyList());

        // Act
        List<Map<String, Object>> milestoneData = projectStatsService.getMilestoneProgress("PRJ_001");

        // Assert
        assertEquals(1, milestoneData.size());
        assertEquals(0, milestoneData.get(0).get("total"));
        // percent is 0 which could be Integer or Long depending on Math.round return
        assertEquals(0L, milestoneData.get(0).get("percent"));
    }

    @Test
    void testCompareProjects_shouldCalculateSchedulePerformanceAndDefectDensity() {
        // Arrange
        Task completedTask = new Task();
        completedTask.setId(1L);
        completedTask.setProjectId("PRJ_001");
        completedTask.setStatus("DONE");
        completedTask.setProgress(100);
        completedTask.setDueDate(LocalDate.now().minusDays(1));
        completedTask.setCompletionDate(LocalDateTime.now().minusDays(2));

        Task bugTask = new Task();
        bugTask.setId(2L);
        bugTask.setProjectId("PRJ_001");
        bugTask.setType("BUG");
        bugTask.setProgress(0); // Set progress to avoid NPE

        when(projectMapper.findByProjectId("PRJ_001")).thenReturn(testProject);
        when(taskMapper.findByProjectId("PRJ_001")).thenReturn(Arrays.asList(completedTask, bugTask));

        // Act
        List<Map<String, Object>> comparisons = projectStatsService.compareProjects(Collections.singletonList("PRJ_001"));

        // Assert
        assertEquals(1, comparisons.size());
        assertNotNull(comparisons.get(0).get("schedulePerformance"));
        assertNotNull(comparisons.get(0).get("defectDensity"));
    }
}
