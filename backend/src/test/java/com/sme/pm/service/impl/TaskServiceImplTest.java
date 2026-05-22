package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.event.TaskAssignedEvent;
import com.sme.pm.event.TaskStatusChangedEvent;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskStatusMapper taskStatusMapper;

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
        task.setParentId(null);

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

        Task childTask = new Task();
        childTask.setTitle("Child Task");
        childTask.setParentId(1L);

        when(taskMapper.findById(1L)).thenReturn(parentTask);
        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        Task result = taskService.create(childTask);

        assertEquals(3, result.getDepth());
        verify(taskMapper).insert(childTask);
    }

    @Test
    void create_shouldThrowException_whenDepthExceeds4() {
        Task parentTask = new Task();
        parentTask.setId(1L);
        parentTask.setDepth(4); // Already at max depth

        Task childTask = new Task();
        childTask.setTitle("Deep Task");
        childTask.setParentId(1L);

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
        task.setStatus(2);

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
        Integer oldStatusId = 10;
        Long newStatusId = 20L;

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setStatus(oldStatusId);
        task.setAssigneeId(5L);
        task.setProjectId("PRJ_TEST");

        TaskStatus currentStatus = new TaskStatus();
        currentStatus.setId(oldStatusId.longValue());
        currentStatus.setCategory("todo");

        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(newStatusId);
        newStatus.setName("In Progress");
        newStatus.setCategory("IN_PROGRESS");

        when(taskMapper.findById(taskId)).thenReturn(task);
        when(taskStatusMapper.selectById(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg.equals(oldStatusId) || arg.equals(oldStatusId.longValue())) {
                return currentStatus;
            } else if (arg.equals(newStatusId) || arg.equals(newStatusId.intValue())) {
                return newStatus;
            }
            return null;
        });
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        taskService.updateStatus(taskId, newStatusId);

        ArgumentCaptor<TaskStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(TaskStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        TaskStatusChangedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(5L, capturedEvent.getUserId());
        assertEquals(taskId, capturedEvent.getTaskId());
        assertEquals(oldStatusId.longValue(), capturedEvent.getOldStatusId());
        assertEquals(newStatusId, capturedEvent.getNewStatusId());
    }

    @Test
    void updateStatus_shouldSetInProgressSince_whenMovingToInProgress() {
        Long taskId = 1L;
        Integer oldStatusId = 10;
        Long newStatusId = 20L;

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setStatus(oldStatusId);
        task.setAssigneeId(5L);
        task.setProjectId("PRJ_TEST");
        task.setInProgressSince(null); // Not yet in progress

        TaskStatus currentStatus = new TaskStatus();
        currentStatus.setId(oldStatusId.longValue());
        currentStatus.setCategory("TODO");

        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(newStatusId);
        newStatus.setName("In Progress");
        newStatus.setCategory("IN_PROGRESS");

        when(taskMapper.findById(taskId)).thenReturn(task);
        when(taskStatusMapper.selectById(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg.equals(oldStatusId) || arg.equals(oldStatusId.longValue())) {
                return currentStatus;
            } else if (arg.equals(newStatusId) || arg.equals(newStatusId.intValue())) {
                return newStatus;
            }
            return null;
        });
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        Task result = taskService.updateStatus(taskId, newStatusId);

        assertNotNull(result.getInProgressSince());
    }

    @Test
    void updateStatus_shouldSetCompletionDateAndProgress_whenMovingToDone() {
        Long taskId = 1L;
        Integer oldStatusId = 20;
        Long newStatusId = 30L;

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setStatus(oldStatusId);
        task.setAssigneeId(5L);
        task.setProjectId("PRJ_TEST");
        task.setProgress(50);
        task.setInProgressSince(java.time.LocalDateTime.now());

        TaskStatus currentStatus = new TaskStatus();
        currentStatus.setId(oldStatusId.longValue());
        currentStatus.setCategory("IN_PROGRESS");

        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(newStatusId);
        newStatus.setName("Done");
        newStatus.setCategory("DONE");

        when(taskMapper.findById(taskId)).thenReturn(task);
        when(taskStatusMapper.selectById(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg.equals(oldStatusId) || arg.equals(oldStatusId.longValue())) {
                return currentStatus;
            } else if (arg.equals(newStatusId) || arg.equals(newStatusId.intValue())) {
                return newStatus;
            }
            return null;
        });
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        Task result = taskService.updateStatus(taskId, newStatusId);

        assertEquals(100, result.getProgress());
        assertNotNull(result.getCompletionDate());
        assertNull(result.getInProgressSince());
    }

    @Test
    void canTransitionTo_shouldCheckBlockingDependencies() {
        Long taskId = 1L;
        Long targetStatusId = 20L;

        when(taskDependencyService.canTransitionTo(taskId, targetStatusId)).thenReturn(true);

        boolean result = taskService.canTransitionTo(taskId, targetStatusId);

        assertTrue(result);
        verify(taskDependencyService).canTransitionTo(taskId, targetStatusId);
    }
}
