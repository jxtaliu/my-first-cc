package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskMapper);
    }

    @Test
    void create_shouldSetDepthTo1_whenParentIdIsNull() {
        Task task = new Task();
        task.setTitle("Root Task");
        task.setParentId(null);

        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        Task result = taskService.create(task);

        assertEquals(1, result.getDepth());
        verify(taskMapper).insert(task);
    }

    @Test
    void create_shouldSetDepthBasedOnParent_whenParentExists() {
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
    void create_shouldThrowException_whenMaxDepthExceeded() {
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
}
