package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.service.TaskService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskController.
 *
 * <p>Test scenarios covered:</p>
 * <ul>
 *   <li>Create a new task</li>
 *   <li>List tasks by sprintId, projectId, or assigneeId</li>
 *   <li>Get task by ID</li>
 *   <li>Update task</li>
 *   <li>Delete task</li>
 *   <li>Move task to another sprint/status</li>
 *   <li>Batch move tasks</li>
 *   <li>Assign task to user</li>
 *   <li>Update task status</li>
 *   <li>Check if task can transition to target status</li>
 *   <li>Get/add/delete task comments</li>
 *   <li>Get/add/delete task attachments</li>
 *   <li>Get/add/delete task dependencies</li>
 *   <li>Count tasks by status</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    private TaskController taskController;

    @BeforeEach
    void setUp() {
        taskController = new TaskController(taskService);
    }

    // ==================== Create Task Tests ====================

    @Test
    void create_shouldReturnCreatedTask() {
        // Arrange
        Task task = createTask(1L, "Task 1", "TODO");
        when(taskService.create(any(Task.class))).thenReturn(task);

        // Act
        Result<Task> result = taskController.create(task, 1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(task, result.getData());
        verify(taskService).create(task);
        assertEquals(1L, task.getAssigneeId());
    }

    // ==================== List Tasks Tests ====================

    @Test
    void list_shouldReturnTasksBySprintId() {
        // Arrange
        List<Task> tasks = Arrays.asList(createTask(1L, "Task 1", "TODO"),
                                         createTask(2L, "Task 2", "IN_PROGRESS"));
        when(taskService.listBySprint(1L)).thenReturn(tasks);

        // Act
        Result<List<Task>> result = taskController.list(1L, null, null);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(2, result.getData().size());
        verify(taskService).listBySprint(1L);
    }

    @Test
    void list_shouldReturnTasksByProjectId() {
        // Arrange
        List<Task> tasks = Collections.singletonList(createTask(1L, "Task 1", "TODO"));
        when(taskService.listByProject("PRJ_001")).thenReturn(tasks);

        // Act
        Result<List<Task>> result = taskController.list(null, "PRJ_001", null);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(1, result.getData().size());
        verify(taskService).listByProject("PRJ_001");
    }

    @Test
    void list_shouldReturnTasksByAssigneeId() {
        // Arrange
        List<Task> tasks = Collections.singletonList(createTask(1L, "Task 1", "DONE"));
        when(taskService.listByAssignee(1L)).thenReturn(tasks);

        // Act
        Result<List<Task>> result = taskController.list(null, null, 1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(1, result.getData().size());
        verify(taskService).listByAssignee(1L);
    }

    @Test
    void list_shouldReturnError_whenNoFilterProvided() {
        // Act
        Result<List<Task>> result = taskController.list(null, null, null);

        // Assert
        assertFalse(result.getCode() == 200);
        assertEquals("Must provide sprintId, projectId, or assigneeId", result.getMessage());
    }

    // ==================== Get Task By ID Tests ====================

    @Test
    void getById_shouldReturnTask() {
        // Arrange
        Task task = createTask(1L, "Task 1", "TODO");
        when(taskService.getById(1L)).thenReturn(task);

        // Act
        Result<Task> result = taskController.getById(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(task, result.getData());
        verify(taskService).getById(1L);
    }

    @Test
    void getById_shouldReturnNull_whenTaskNotFound() {
        // Arrange
        when(taskService.getById(999L)).thenReturn(null);

        // Act
        Result<Task> result = taskController.getById(999L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertNull(result.getData());
    }

    // ==================== Update Task Tests ====================

    @Test
    void update_shouldReturnUpdatedTask() {
        // Arrange
        Task task = createTask(1L, "Updated Task", "IN_PROGRESS");
        when(taskService.update(any(Task.class))).thenReturn(task);

        // Act
        Result<Task> result = taskController.update(1L, task);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(task, result.getData());
        assertEquals(1L, task.getId());
        verify(taskService).update(task);
    }

    // ==================== Delete Task Tests ====================

    @Test
    void delete_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = taskController.delete(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        verify(taskService).delete(1L);
    }

    // ==================== Move Task Tests ====================

    @Test
    void move_shouldReturnMovedTask() {
        // Arrange
        Task task = createTask(1L, "Task 1", "IN_PROGRESS");
        task.setSprintId(2L);
        when(taskService.move(any(Task.class))).thenReturn(task);

        Task moveRequest = createTask(1L, "Task 1", "IN_PROGRESS");
        moveRequest.setSprintId(2L);

        // Act
        Result<Task> result = taskController.move(1L, moveRequest);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(2L, result.getData().getSprintId());
        verify(taskService).move(any(Task.class));
    }

    @Test
    void batchMove_shouldReturnMoveResult() {
        // Arrange
        Map<String, Object> moveResult = new HashMap<>();
        moveResult.put("successCount", 2);
        moveResult.put("failCount", 0);
        moveResult.put("errors", Collections.emptyList());

        when(taskService.batchMove(any(), eq("IN_PROGRESS"), eq(1L))).thenReturn(moveResult);

        Map<String, Object> request = new HashMap<>();
        request.put("taskIds", Arrays.asList(1, 2));
        request.put("targetStatus", "IN_PROGRESS");
        request.put("sprintId", 1L);

        // Act
        Result<Map<String, Object>> result = taskController.batchMove(request, "PRJ_001");

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(2, result.getData().get("successCount"));
    }

    // ==================== Assign Task Tests ====================

    @Test
    void assign_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = taskController.assign(1L, 2L);

        // Assert
        assertTrue(result.getCode() == 200);
        verify(taskService).assign(1L, 2L);
    }

    // ==================== Update Status Tests ====================

    @Test
    void updateStatus_shouldReturnTaskWithNewStatus() {
        // Arrange
        Task task = createTask(1L, "Task 1", "DONE");
        when(taskService.updateStatus(1L, "DONE")).thenReturn(task);

        // Act
        Result<Task> result = taskController.updateStatus(1L, "DONE");

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals("DONE", result.getData().getStatus());
        verify(taskService).updateStatus(1L, "DONE");
    }

    // ==================== Can Transition Tests ====================

    @Test
    void canTransitionTo_shouldReturnTrue() {
        // Arrange
        when(taskService.canTransitionTo(1L, "DONE")).thenReturn(true);

        // Act
        Result<Boolean> result = taskController.canTransitionTo(1L, "DONE");

        // Assert
        assertTrue(result.getCode() == 200);
        assertTrue(result.getData());
    }

    @Test
    void canTransitionTo_shouldReturnFalse() {
        // Arrange
        when(taskService.canTransitionTo(1L, "DONE")).thenReturn(false);

        // Act
        Result<Boolean> result = taskController.canTransitionTo(1L, "DONE");

        // Assert
        assertTrue(result.getCode() == 200);
        assertFalse(result.getData());
    }

    // ==================== Comments Tests ====================

    @Test
    void getComments_shouldReturnCommentList() {
        // Arrange
        TaskComment comment1 = new TaskComment();
        comment1.setId(1L);
        comment1.setContent("Comment 1");

        TaskComment comment2 = new TaskComment();
        comment2.setId(2L);
        comment2.setContent("Comment 2");

        when(taskService.getComments(1L)).thenReturn(Arrays.asList(comment1, comment2));

        // Act
        Result<List<TaskComment>> result = taskController.getComments(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(2, result.getData().size());
    }

    @Test
    void addComment_shouldCallServiceAndReturnSuccess() {
        // Arrange
        TaskComment comment = new TaskComment();
        comment.setContent("New comment");

        // Act
        Result<Void> result = taskController.addComment(1L, comment);

        // Assert
        assertTrue(result.getCode() == 200);
        verify(taskService).addComment(eq(1L), any(TaskComment.class));
    }

    // ==================== Attachments Tests ====================

    @Test
    void getAttachments_shouldReturnAttachmentList() {
        // Arrange
        TaskAttachment attachment = new TaskAttachment();
        attachment.setId(1L);
        attachment.setFileName("test.pdf");

        when(taskService.getAttachments(1L)).thenReturn(Collections.singletonList(attachment));

        // Act
        Result<List<TaskAttachment>> result = taskController.getAttachments(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(1, result.getData().size());
    }

    @Test
    void addAttachment_shouldCallServiceAndReturnSuccess() {
        // Arrange
        TaskAttachment attachment = new TaskAttachment();
        attachment.setFileName("new.pdf");

        // Act
        Result<Void> result = taskController.addAttachment(1L, attachment);

        // Assert
        assertTrue(result.getCode() == 200);
        verify(taskService).addAttachment(eq(1L), any(TaskAttachment.class));
    }

    @Test
    void deleteAttachment_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = taskController.deleteAttachment(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        verify(taskService).deleteAttachment(1L);
    }

    // ==================== Dependencies Tests ====================

    @Test
    void getDependencies_shouldReturnDependencyList() {
        // Arrange
        TaskDependency dependency = new TaskDependency();
        dependency.setId(1L);
        dependency.setTaskId(1L);
        dependency.setDependsOnTaskId(2L);

        when(taskService.getDependencies(1L)).thenReturn(Collections.singletonList(dependency));

        // Act
        Result<List<TaskDependency>> result = taskController.getDependencies(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(1, result.getData().size());
    }

    @Test
    void getBlockingDependencies_shouldReturnBlockingList() {
        // Arrange
        TaskDependency dependency = new TaskDependency();
        dependency.setId(1L);
        dependency.setTaskId(2L);
        dependency.setDependsOnTaskId(1L);

        when(taskService.getBlockingDependencies(1L)).thenReturn(Collections.singletonList(dependency));

        // Act
        Result<List<TaskDependency>> result = taskController.getBlockingDependencies(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(1, result.getData().size());
    }

    @Test
    void countBlockingDependencies_shouldReturnCount() {
        // Arrange
        when(taskService.countBlockingDependencies(1L)).thenReturn(3);

        // Act
        Result<Integer> result = taskController.countBlockingDependencies(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(3, result.getData());
    }

    @Test
    void addDependency_shouldCallServiceAndReturnSuccess() {
        // Arrange
        TaskDependency dependency = new TaskDependency();
        dependency.setDependsOnTaskId(2L);

        // Act
        Result<Void> result = taskController.addDependency(1L, dependency);

        // Assert
        assertTrue(result.getCode() == 200);
        verify(taskService).addDependency(eq(1L), any(TaskDependency.class));
    }

    @Test
    void removeDependency_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = taskController.removeDependency(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        verify(taskService).removeDependency(1L);
    }

    // ==================== Count By Status Tests ====================

    @Test
    void countByStatus_shouldReturnCount() {
        // Arrange
        when(taskService.countByStatusId(1)).thenReturn(5);

        // Act
        Result<Long> result = taskController.countByStatus(1L);

        // Assert
        assertTrue(result.getCode() == 200);
        assertEquals(5L, result.getData());
    }

    // ==================== Helper Methods ====================

    private Task createTask(Long id, String title, String status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setStatus(status);
        task.setProjectId("PRJ_001");
        task.setType("TASK");
        return task;
    }
}
