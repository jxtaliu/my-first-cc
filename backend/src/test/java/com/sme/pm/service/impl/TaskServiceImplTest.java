package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.event.TaskAssignedEvent;
import com.sme.pm.event.TaskStatusChangedEvent;
import com.sme.pm.mapper.TaskIdSequenceMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.mapper.TaskStatusMapper;
import com.sme.pm.service.ITaskCommentService;
import com.sme.pm.service.ITaskAttachmentService;
import com.sme.pm.service.ITaskDependencyService;
import com.sme.pm.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskStatusMapper taskStatusMapper;

    @Mock
    private TaskIdSequenceMapper taskIdSequenceMapper;

    @Mock
    private ITaskCommentService taskCommentService;

    @Mock
    private ITaskAttachmentService taskAttachmentService;

    @Mock
    private ITaskDependencyService taskDependencyService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(
            taskMapper,
            taskStatusMapper,
            taskIdSequenceMapper,
            taskCommentService,
            taskAttachmentService,
            taskDependencyService,
            eventPublisher
        );
    }

    @Test
    void create_shouldSetDepth1_whenNoParent() {
        Task task = new Task();
        task.setTitle("Root Task");
        task.setType("TASK");
        task.setParentId(null);
        task.setProjectId("PRJ_001"); // Required field

        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        Task result = taskService.create(task);

        assertEquals(1, result.getDepth());
        verify(taskMapper).insert(task);
    }

    @Test
    void create_shouldSetDepthParentPlus1_whenHasParent() {
        Task parentTask = new Task();
        parentTask.setId(1L);
        parentTask.setDepth(2);
        parentTask.setProjectId("PRJ_001");

        Task childTask = new Task();
        childTask.setTitle("Child Task");
        childTask.setType("TASK");
        childTask.setParentId(1L);
        childTask.setProjectId("PRJ_001"); // Required field

        when(taskMapper.findById(1L)).thenReturn(parentTask);
        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        Task result = taskService.create(childTask);

        assertEquals(3, result.getDepth());
        verify(taskMapper).insert(childTask);
    }

    @Test
    void create_shouldThrowException_whenDepthExceedsMax() {
        Task parentTask = new Task();
        parentTask.setId(1L);
        parentTask.setDepth(5); // Already at MAX_DEPTH (5)

        Task childTask = new Task();
        childTask.setTitle("Deep Task");
        childTask.setType("TASK");
        childTask.setParentId(1L);
        childTask.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(parentTask);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.create(childTask)
        );

        assertTrue(exception.getMessage().contains("Maximum task hierarchy depth"));
    }

    @Test
    void update_shouldUpdateTaskAndReturn() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Updated Title");
        task.setStatus("TODO");

        when(taskMapper.updateById(task)).thenReturn(1);

        Task result = taskService.update(task);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(taskMapper).updateById(task);
    }

    @Test
    void delete_shouldCallDeleteById() {
        Long taskId = 1L;

        taskService.delete(taskId);

        verify(taskMapper).deleteById(taskId);
    }

    @Test
    void getById_shouldReturnTask() {
        Long taskId = 1L;
        Task expectedTask = new Task();
        expectedTask.setId(taskId);
        expectedTask.setTitle("Test Task");

        when(taskMapper.findById(taskId)).thenReturn(expectedTask);

        Task result = taskService.getById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        verify(taskMapper).findById(taskId);
    }

    @Test
    void listBySprint_shouldReturnTasks() {
        Long sprintId = 1L;
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        when(taskMapper.findBySprintId(sprintId)).thenReturn(Arrays.asList(task1, task2));

        List<Task> result = taskService.listBySprint(sprintId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskMapper).findBySprintId(sprintId);
    }

    @Test
    void listByParent_shouldReturnChildTasks() {
        Long parentId = 1L;
        Task child1 = new Task();
        child1.setId(2L);
        child1.setParentId(parentId);

        Task child2 = new Task();
        child2.setId(3L);
        child2.setParentId(parentId);

        when(taskMapper.findByParentId(parentId)).thenReturn(Arrays.asList(child1, child2));

        List<Task> result = taskService.listByParent(parentId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskMapper).findByParentId(parentId);
    }

    @Test
    void assign_shouldPublishTaskAssignedEvent() {
        Long taskId = 1L;
        Long userId = 2L;

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setAssigneeId(null);
        task.setProjectId("PRJ_TEST");

        when(taskMapper.findById(taskId)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        taskService.assign(taskId, userId);

        ArgumentCaptor<TaskAssignedEvent> eventCaptor = ArgumentCaptor.forClass(TaskAssignedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        TaskAssignedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(taskId, capturedEvent.getTaskId());
        assertEquals(userId, capturedEvent.getAssigneeId());
        assertEquals("PRJ_TEST", capturedEvent.getRelatedProjectId());
        assertEquals("Task Assigned", capturedEvent.getTitle());
    }

    @Test
    void updateStatus_shouldPublishTaskStatusChangedEvent() {
        Long taskId = 1L;
        String oldStatusCode = "TODO";
        String newStatusCode = "IN_PROGRESS";

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setStatus(oldStatusCode);
        task.setAssigneeId(5L);
        task.setProjectId("PRJ_TEST");

        TaskStatus currentStatus = new TaskStatus();
        currentStatus.setId(10L);
        currentStatus.setCode("TODO");

        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(20L);
        newStatus.setNameEn("In Progress");
        newStatus.setCode("IN_PROGRESS");

        when(taskMapper.findById(taskId)).thenReturn(task);
        // Mock selectOne to return newStatus for IN_PROGRESS query
        when(taskStatusMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
            .thenReturn(newStatus);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        taskService.updateStatus(taskId, newStatusCode);

        ArgumentCaptor<TaskStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(TaskStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        TaskStatusChangedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(5L, capturedEvent.getUserId());
        assertEquals(taskId, capturedEvent.getTaskId());
        assertEquals(oldStatusCode, capturedEvent.getOldStatusCode());
        assertEquals(newStatusCode, capturedEvent.getNewStatusCode());
    }

    @Test
    void updateStatus_shouldSetInProgressSince_whenMovingToInProgress() {
        Long taskId = 1L;
        String oldStatusCode = "TODO";
        String newStatusCode = "IN_PROGRESS";

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setStatus(oldStatusCode);
        task.setAssigneeId(5L);
        task.setProjectId("PRJ_TEST");
        task.setInProgressSince(null); // Not yet in progress

        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(20L);
        newStatus.setNameEn("In Progress");
        newStatus.setCode("IN_PROGRESS");

        when(taskMapper.findById(taskId)).thenReturn(task);
        when(taskStatusMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
            .thenReturn(newStatus);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        Task result = taskService.updateStatus(taskId, newStatusCode);

        assertNotNull(result.getInProgressSince());
    }

    @Test
    void updateStatus_shouldSetCompletionDateAndProgress_whenMovingToDone() {
        Long taskId = 1L;
        String oldStatusCode = "IN_PROGRESS";
        String newStatusCode = "DONE";

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setStatus(oldStatusCode);
        task.setAssigneeId(5L);
        task.setProjectId("PRJ_TEST");
        task.setProgress(50);
        task.setInProgressSince(java.time.LocalDateTime.now());

        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(30L);
        newStatus.setNameEn("Done");
        newStatus.setCode("DONE");

        when(taskMapper.findById(taskId)).thenReturn(task);
        when(taskStatusMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
            .thenReturn(newStatus);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        Task result = taskService.updateStatus(taskId, newStatusCode);

        assertEquals(100, result.getProgress());
        assertNotNull(result.getCompletionDate());
        assertNull(result.getInProgressSince());
    }

    @Test
    void canTransitionTo_shouldCheckBlockingDependencies() {
        Long taskId = 1L;
        String targetStatusCode = "IN_PROGRESS";

        when(taskDependencyService.canTransitionTo(taskId, targetStatusCode)).thenReturn(true);

        boolean result = taskService.canTransitionTo(taskId, targetStatusCode);

        assertTrue(result);
        verify(taskDependencyService).canTransitionTo(taskId, targetStatusCode);
    }

    // ==================== Requirement Tree Tests ====================

    @Test
    void getRequirementTree_shouldReturnEpicsAndFeatures() {
        String projectId = "PRJ_001";
        List<Task> tasks = Arrays.asList(
                createTask(1L, "Epic 1", "EPIC", projectId),
                createTask(2L, "Feature 1", "FEATURE", projectId)
        );

        when(taskMapper.selectList(any())).thenReturn(tasks);

        List<Task> result = taskService.getRequirementTree(projectId);

        assertEquals(2, result.size());
    }

    @Test
    void getRequirementChildren_shouldReturnDirectChildren() {
        Long parentId = 1L;
        List<Task> children = Arrays.asList(
                createTask(2L, "Story 1", "STORY", "PRJ_001"),
                createTask(3L, "Story 2", "STORY", "PRJ_001")
        );

        when(taskMapper.selectList(any())).thenReturn(children);

        List<Task> result = taskService.getRequirementChildren(parentId);

        assertEquals(2, result.size());
    }

    @Test
    void getBugs_shouldReturnBugsForProject() {
        String projectId = "PRJ_001";
        List<Task> bugs = Arrays.asList(
                createTask(1L, "Bug 1", "BUG", projectId),
                createTask(2L, "Bug 2", "BUG", projectId)
        );

        when(taskMapper.selectList(any())).thenReturn(bugs);

        List<Task> result = taskService.getBugs(projectId);

        assertEquals(2, result.size());
        assertEquals("BUG", result.get(0).getType());
    }

    @Test
    void updateBugStatus_shouldUpdateBugStatusId() {
        Long taskId = 1L;
        Long bugStatusId = 5L;

        Task task = new Task();
        task.setId(taskId);
        task.setType("BUG");
        task.setProjectId("PRJ_001");

        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        taskService.updateBugStatus(taskId, bugStatusId);

        assertEquals(bugStatusId, task.getBugStatusId());
        verify(taskMapper).updateById(task);
    }

    @Test
    void moveRequirement_shouldUpdateParentAndDepth_whenNewParentProvided() {
        Long taskId = 1L;
        Long newParentId = 10L;

        Task task = new Task();
        task.setId(taskId);
        task.setDepth(1);

        Task newParent = new Task();
        newParent.setId(newParentId);
        newParent.setDepth(2);

        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(taskMapper.selectById(newParentId)).thenReturn(newParent);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        taskService.moveRequirement(taskId, newParentId, null);

        assertEquals(newParentId, task.getParentId());
        assertEquals(3, task.getDepth());
        verify(taskMapper).updateById(task);
    }

    @Test
    void moveRequirement_shouldResetToDepth1_whenNewParentIsNull() {
        Long taskId = 1L;

        Task task = new Task();
        task.setId(taskId);
        task.setDepth(3);
        task.setParentId(10L);

        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        taskService.moveRequirement(taskId, null, null);

        assertNull(task.getParentId());
        assertEquals(1, task.getDepth());
        verify(taskMapper).updateById(task);
    }

    @Test
    void moveRequirement_shouldDoNothing_whenTaskNotFound() {
        Long taskId = 999L;
        Long newParentId = 10L;

        when(taskMapper.selectById(taskId)).thenReturn(null);

        taskService.moveRequirement(taskId, newParentId, null);

        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void getRequirementSubtree_shouldReturnEmptyList_whenNoChildren() {
        Long parentId = 1L;

        when(taskMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        List<Task> result = taskService.getRequirementSubtree(parentId);

        assertTrue(result.isEmpty());
    }

    // ==================== Move Task Tests ====================

    // Note: move() uses LambdaUpdateWrapper which requires MyBatis-Plus entity metadata.
    // LambdaUpdateWrapper cannot be properly mocked in unit tests.
    // Successful move cases are covered by batchMove tests.

    @Test
    void move_shouldThrowException_whenTaskNotFound() {
        // Arrange
        Task task = new Task();
        task.setId(999L);
        task.setType("TASK");
        task.setSprintId(100L);

        when(taskMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.move(task)
        );
        assertTrue(exception.getMessage().contains("Task not found"));
    }

    @Test
    void move_shouldThrowException_whenMovingEpicToSprint() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("EPIC");
        task.setSprintId(null);

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setType("EPIC");
        existingTask.setSprintId(null);

        when(taskMapper.findById(1L)).thenReturn(existingTask);

        task.setSprintId(100L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.move(task)
        );
        assertTrue(exception.getMessage().contains("EPIC类型的任务不允许直接设置冲刺"));
    }

    @Test
    void move_shouldThrowException_whenMovingFeatureToSprint() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("FEATURE");
        task.setSprintId(null);

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setType("FEATURE");
        existingTask.setSprintId(null);

        when(taskMapper.findById(1L)).thenReturn(existingTask);

        task.setSprintId(100L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.move(task)
        );
        assertTrue(exception.getMessage().contains("FEATURE类型的任务不允许直接设置冲刺"));
    }

    @Test
    void move_shouldThrowException_whenMovingStoryToSprint() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("STORY");
        task.setSprintId(null);

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setType("STORY");
        existingTask.setSprintId(null);

        when(taskMapper.findById(1L)).thenReturn(existingTask);

        task.setSprintId(100L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.move(task)
        );
        assertTrue(exception.getMessage().contains("STORY类型的任务不允许直接设置冲刺"));
    }

    // ==================== BatchMove Tests ====================

    @Test
    void batchMove_shouldReturnZeroCounts_whenTaskIdsIsNull() {
        // Act
        Map<String, Object> result = taskService.batchMove(null, "TODO", 1L);

        // Assert
        assertEquals(0, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
    }

    @Test
    void batchMove_shouldReturnZeroCounts_whenTaskIdsIsEmpty() {
        // Act
        Map<String, Object> result = taskService.batchMove(Collections.emptyList(), "TODO", 1L);

        // Assert
        assertEquals(0, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
    }

    @Test
    void batchMove_shouldMoveSingleTaskToSprint() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("TASK");
        task.setSprintId(null);
        task.setStatus("TODO");
        task.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, 100L);

        // Assert
        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
        assertEquals(100L, task.getSprintId());
    }

    @Test
    void batchMove_shouldMoveMultipleTasksToSprint() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setType("TASK");
        task1.setSprintId(null);
        task1.setProjectId("PRJ_001");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setType("TASK");
        task2.setSprintId(null);
        task2.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(task1);
        when(taskMapper.findById(2L)).thenReturn(task2);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        List<Long> taskIds = Arrays.asList(1L, 2L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, 100L);

        // Assert
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
        assertEquals(100L, task1.getSprintId());
        assertEquals(100L, task2.getSprintId());
    }

    @Test
    void batchMove_shouldMoveTaskToBacklog_whenSprintIdIsNull() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("TASK");
        task.setSprintId(100L);
        task.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, null);

        // Assert
        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
        assertNull(task.getSprintId());
    }

    @Test
    void batchMove_shouldSkipEpicTask_whenMovingToSprint() {
        // Arrange
        Task epicTask = new Task();
        epicTask.setId(1L);
        epicTask.setType("EPIC");
        epicTask.setSprintId(null);
        epicTask.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(epicTask);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, 100L);

        // Assert
        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        assertNull(epicTask.getSprintId()); // Epic should NOT have sprintId changed
    }

    @Test
    void batchMove_shouldSkipFeatureTask_whenMovingToSprint() {
        // Arrange
        Task featureTask = new Task();
        featureTask.setId(1L);
        featureTask.setType("FEATURE");
        featureTask.setSprintId(null);
        featureTask.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(featureTask);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, 100L);

        // Assert
        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        assertNull(featureTask.getSprintId()); // Feature should NOT have sprintId changed
    }

    @Test
    void batchMove_shouldSkipStoryTask_whenMovingToSprint() {
        // Arrange
        Task storyTask = new Task();
        storyTask.setId(1L);
        storyTask.setType("STORY");
        storyTask.setSprintId(null);
        storyTask.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(storyTask);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, 100L);

        // Assert
        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        assertNull(storyTask.getSprintId()); // Story should NOT have sprintId changed
    }

    @Test
    void batchMove_shouldSkipTaskNotFound() {
        // Arrange
        when(taskMapper.findById(999L)).thenReturn(null);

        List<Long> taskIds = Collections.singletonList(999L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, 100L);

        // Assert
        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get("errors");
        assertTrue(errors.get(0).contains("Task not found: 999"));
    }

    @Test
    void batchMove_shouldUpdateStatus_whenTargetStatusProvided() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("TASK");
        task.setSprintId(100L);
        task.setStatus("TODO");
        task.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, "DONE", 100L);

        // Assert
        assertEquals(1, result.get("successCount"));
        assertEquals("DONE", task.getStatus());
    }

    @Test
    void batchMove_shouldSetCompletionDate_whenMovingToDone() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("TASK");
        task.setSprintId(100L);
        task.setStatus("IN_PROGRESS");
        task.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, "DONE", 100L);

        // Assert
        assertEquals(1, result.get("successCount"));
        assertEquals("DONE", task.getStatus());
        assertEquals(100, task.getProgress());
        assertNotNull(task.getCompletionDate());
        assertNull(task.getInProgressSince());
    }

    @Test
    void batchMove_shouldSetInProgressSince_whenMovingToInProgress() {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setType("TASK");
        task.setSprintId(100L);
        task.setStatus("TODO");
        task.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        List<Long> taskIds = Collections.singletonList(1L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, "IN_PROGRESS", 100L);

        // Assert
        assertEquals(1, result.get("successCount"));
        assertEquals("IN_PROGRESS", task.getStatus());
        assertNotNull(task.getInProgressSince());
    }

    @Test
    void batchMove_shouldHandleMixedSuccessAndFailure() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setType("TASK");
        task1.setSprintId(null);
        task1.setProjectId("PRJ_001");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setType("EPIC"); // Will fail - epic cannot change sprint
        task2.setSprintId(null);
        task2.setProjectId("PRJ_001");

        Task task3 = new Task();
        task3.setId(3L);
        task3.setType("TASK");
        task3.setSprintId(null);
        task3.setProjectId("PRJ_001");

        when(taskMapper.findById(1L)).thenReturn(task1);
        when(taskMapper.findById(2L)).thenReturn(task2);
        when(taskMapper.findById(3L)).thenReturn(task3);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        List<Long> taskIds = Arrays.asList(1L, 2L, 3L);

        // Act
        Map<String, Object> result = taskService.batchMove(taskIds, null, 100L);

        // Assert
        assertEquals(2, result.get("successCount")); // task1 and task3 succeed
        assertEquals(1, result.get("failedCount")); // task2 (EPIC) fails
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get("errors");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("EPIC"));
    }

    // ==================== Additional create tests for error paths ====================

    @Test
    void create_shouldThrowException_whenProjectIdIsEmpty() {
        Task task = new Task();
        task.setProjectId("");
        task.setType("TASK");
        task.setTitle("Test");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.create(task)
        );
        assertTrue(exception.getMessage().contains("projectId不能为空"));
    }

    @Test
    void create_shouldThrowException_whenTitleIsEmpty() {
        Task task = new Task();
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        task.setTitle("");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.create(task)
        );
        assertTrue(exception.getMessage().contains("title不能为空"));
    }

    @Test
    void create_shouldGenerateTaskId_whenNotProvided() {
        Task task = new Task();
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        task.setTitle("Test Task");
        task.setTaskId(null);

        when(taskIdSequenceMapper.insertIfNotExists(any(), any())).thenReturn(1);
        when(taskIdSequenceMapper.incrementSeq(any(), any())).thenReturn(1);
        when(taskIdSequenceMapper.getCurrentSeq(any(), any())).thenReturn(1);
        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        Task result = taskService.create(task);

        assertNotNull(result.getTaskId());
        assertTrue(result.getTaskId().startsWith("TSK"));
    }

    @Test
    void create_shouldThrowException_whenDuplicateTaskId() {
        Task task = new Task();
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        task.setTitle("Test Task");
        task.setTaskId("TSK001");

        when(taskMapper.findByTaskId("TSK001")).thenReturn(new Task());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.create(task)
        );
        assertTrue(exception.getMessage().contains("任务ID已存在"));
    }

    @Test
    void create_shouldSetDefaultStatus_whenNotSpecified() {
        Task task = new Task();
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        task.setTitle("Test Task");
        task.setStatus(null);

        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        Task result = taskService.create(task);

        assertEquals("TODO", result.getStatus());
    }

    @Test
    void create_shouldSetDefaultProgress_whenNotSpecified() {
        Task task = new Task();
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        task.setTitle("Test Task");
        task.setProgress(null);

        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        Task result = taskService.create(task);

        assertEquals(0, result.getProgress());
    }

    // ==================== update tests ====================

    @Test
    void update_shouldThrowException_whenIdIsNull() {
        Task task = new Task();
        task.setId(null);
        task.setTitle("Test");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.update(task)
        );
        assertTrue(exception.getMessage().contains("id不能为空"));
    }

    @Test
    void update_shouldThrowException_whenTitleIsEmptyString() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskService.update(task)
        );
        assertTrue(exception.getMessage().contains("title不能为空字符串"));
    }

    // ==================== Comment tests ====================

    @Test
    void addComment_shouldSetTaskIdAndSave() {
        Long taskId = 1L;
        TaskComment comment = new TaskComment();
        comment.setContent("Test comment");

        when(taskCommentService.save(any(TaskComment.class))).thenReturn(true);

        taskService.addComment(taskId, comment);

        assertEquals(taskId, comment.getTaskId());
        verify(taskCommentService).save(comment);
    }

    @Test
    void getComments_shouldReturnComments() {
        Long taskId = 1L;
        TaskComment comment = new TaskComment();
        comment.setId(1L);
        comment.setContent("Test comment");

        when(taskCommentService.findByTaskId(taskId)).thenReturn(Collections.singletonList(comment));

        List<TaskComment> result = taskService.getComments(taskId);

        assertEquals(1, result.size());
        verify(taskCommentService).findByTaskId(taskId);
    }

    // ==================== Attachment tests ====================

    @Test
    void addAttachment_shouldSetTaskIdAndUpload() {
        Long taskId = 1L;
        TaskAttachment attachment = new TaskAttachment();
        attachment.setFileName("test.pdf");

        taskService.addAttachment(taskId, attachment);

        assertEquals(taskId, attachment.getTaskId());
        verify(taskAttachmentService).uploadAttachment(attachment);
    }

    @Test
    void getAttachments_shouldReturnAttachments() {
        Long taskId = 1L;
        TaskAttachment attachment = new TaskAttachment();
        attachment.setId(1L);

        when(taskAttachmentService.findByTaskId(taskId)).thenReturn(Collections.singletonList(attachment));

        List<TaskAttachment> result = taskService.getAttachments(taskId);

        assertEquals(1, result.size());
        verify(taskAttachmentService).findByTaskId(taskId);
    }

    @Test
    void deleteAttachment_shouldCallDelete() {
        Long attachmentId = 1L;

        taskService.deleteAttachment(attachmentId);

        verify(taskAttachmentService).deleteAttachment(attachmentId);
    }

    // ==================== Dependency tests ====================

    @Test
    void addDependency_shouldSetTaskIdAndSave() {
        Long taskId = 1L;
        TaskDependency dependency = new TaskDependency();
        dependency.setDependsOnTaskId(2L);

        when(taskDependencyService.findByDependsOnTaskId(taskId)).thenReturn(Collections.emptyList());
        when(taskMapper.findById(taskId)).thenReturn(new Task());
        when(taskDependencyService.save(any(TaskDependency.class))).thenReturn(true);

        taskService.addDependency(taskId, dependency);

        assertEquals(taskId, dependency.getTaskId());
        verify(taskDependencyService).save(dependency);
    }

    @Test
    void removeDependency_shouldCallRemove() {
        Long dependencyId = 1L;

        taskService.removeDependency(dependencyId);

        verify(taskDependencyService).removeById(dependencyId);
    }

    @Test
    void getDependencies_shouldReturnDependencies() {
        Long taskId = 1L;
        TaskDependency dependency = new TaskDependency();
        dependency.setId(1L);

        when(taskDependencyService.findByTaskId(taskId)).thenReturn(Collections.singletonList(dependency));

        List<TaskDependency> result = taskService.getDependencies(taskId);

        assertEquals(1, result.size());
        verify(taskDependencyService).findByTaskId(taskId);
    }

    @Test
    void getBlockingDependencies_shouldReturnBlockingDeps() {
        Long taskId = 1L;
        TaskDependency dependency = new TaskDependency();
        dependency.setId(1L);

        when(taskDependencyService.findByDependsOnTaskId(taskId))
            .thenReturn(Collections.singletonList(dependency));

        List<TaskDependency> result = taskService.getBlockingDependencies(taskId);

        assertEquals(1, result.size());
        verify(taskDependencyService).findByDependsOnTaskId(taskId);
    }

    @Test
    void countBlockingDependencies_shouldReturnCount() {
        Long taskId = 1L;

        when(taskDependencyService.countBlockingDependencies(taskId)).thenReturn(5);

        int result = taskService.countBlockingDependencies(taskId);

        assertEquals(5, result);
        verify(taskDependencyService).countBlockingDependencies(taskId);
    }

    // ==================== List methods tests ====================

    @Test
    void listByProject_shouldReturnTasks() {
        String projectId = "PRJ_001";
        Task task = new Task();
        task.setId(1L);

        when(taskMapper.findByProjectIdWithUser(projectId)).thenReturn(Collections.singletonList(task));

        List<Task> result = taskService.listByProject(projectId);

        assertEquals(1, result.size());
        verify(taskMapper).findByProjectIdWithUser(projectId);
    }

    @Test
    void listByAssignee_shouldReturnTasks() {
        Long assigneeId = 1L;
        Task task = new Task();
        task.setId(1L);

        when(taskMapper.findByAssigneeId(assigneeId)).thenReturn(Collections.singletonList(task));

        List<Task> result = taskService.listByAssignee(assigneeId);

        assertEquals(1, result.size());
        verify(taskMapper).findByAssigneeId(assigneeId);
    }

    // ==================== countByStatusId test ====================

    @Test
    void countByStatusId_shouldReturnCount() {
        Integer statusId = 5;

        when(taskMapper.countByStatusId(statusId)).thenReturn(10);

        int result = taskService.countByStatusId(statusId);

        assertEquals(10, result);
        verify(taskMapper).countByStatusId(statusId);
    }

    // ==================== canTransitionTo test ====================

    @Test
    void canTransitionTo_shouldDelegateToDependencyService() {
        Long taskId = 1L;
        String targetStatus = "IN_PROGRESS";

        when(taskDependencyService.canTransitionTo(taskId, targetStatus)).thenReturn(true);

        boolean result = taskService.canTransitionTo(taskId, targetStatus);

        assertTrue(result);
        verify(taskDependencyService).canTransitionTo(taskId, targetStatus);
    }

    // ==================== Requirement Tree tests ====================

    @Test
    void getRequirementTree_shouldBuildCorrectHierarchy() {
        String projectId = "PRJ_001";

        Task epic = new Task();
        epic.setId(1L);
        epic.setType("EPIC");
        epic.setProjectId(projectId);

        Task feature = new Task();
        feature.setId(2L);
        feature.setType("FEATURE");
        feature.setParentId(1L);
        feature.setProjectId(projectId);

        Task story = new Task();
        story.setId(3L);
        story.setType("STORY");
        story.setParentId(2L);
        story.setProjectId(projectId);

        when(taskMapper.selectList(any()))
            .thenReturn(Arrays.asList(epic, feature, story));

        List<Task> result = taskService.getRequirementTree(projectId);

        assertEquals(1, result.size()); // Only epic is root
        assertEquals(1, result.get(0).getChildren().size()); // Epic has feature as child
    }

    @Test
    void getRequirementTree_shouldTreatOrphanTasksAsRoot() {
        String projectId = "PRJ_001";

        Task orphan = new Task();
        orphan.setId(1L);
        orphan.setType("EPIC");
        orphan.setParentId(999L); // Parent doesn't exist
        orphan.setProjectId(projectId);

        when(taskMapper.selectList(any())).thenReturn(Collections.singletonList(orphan));

        List<Task> result = taskService.getRequirementTree(projectId);

        assertEquals(1, result.size()); // Orphan treated as root
    }

    private Task createTask(Long id, String title, String type, String projectId) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setType(type);
        task.setProjectId(projectId);
        task.setDeleted(0);
        return task;
    }
}
