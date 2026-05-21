package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Result<Task> create(@RequestBody Task task, @CurrentUser Long userId) {
        task.setAssigneeId(userId);
        return Result.success(taskService.create(task));
    }

    @GetMapping
    public Result<List<Task>> list(@RequestParam(required = false) Long sprintId,
                                   @RequestParam(required = false) Long projectId) {
        if (sprintId != null) {
            return Result.success(taskService.listBySprint(sprintId));
        }
        return Result.success(taskService.listByProject(projectId));
    }

    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.update(task));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/move")
    public Result<Task> move(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.move(task));
    }

    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long userId) {
        taskService.assign(id, userId);
        return Result.success();
    }

    @GetMapping("/{taskId}/comments")
    public Result<List<Object>> getComments(@PathVariable Long taskId) {
        return Result.success(taskService.getComments(taskId));
    }

    @PostMapping("/{taskId}/comments")
    public Result<Void> addComment(@PathVariable Long taskId, @RequestBody Object comment) {
        taskService.addComment(taskId, comment);
        return Result.success();
    }
}
