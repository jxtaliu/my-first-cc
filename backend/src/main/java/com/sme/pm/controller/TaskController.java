package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprints/{sprintId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Result<Task> create(@PathVariable Long sprintId, @RequestBody Task task) {
        task.setSprintId(sprintId);
        return Result.success(taskService.create(task));
    }

    @GetMapping
    public Result<List<Task>> list(@PathVariable Long sprintId) {
        return Result.success(taskService.listBySprint(sprintId));
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
}
