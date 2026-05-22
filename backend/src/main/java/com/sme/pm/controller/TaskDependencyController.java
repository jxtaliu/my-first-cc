package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.service.ITaskDependencyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task-dependencies")
public class TaskDependencyController {

    private final ITaskDependencyService taskDependencyService;

    public TaskDependencyController(ITaskDependencyService taskDependencyService) {
        this.taskDependencyService = taskDependencyService;
    }

    @GetMapping("/task/{taskId}")
    public Result<List<TaskDependency>> findByTaskId(@PathVariable Long taskId) {
        return Result.success(taskDependencyService.findByTaskId(taskId));
    }

    @GetMapping("/depends-on/{dependsOnTaskId}")
    public Result<List<TaskDependency>> findByDependsOnTaskId(@PathVariable Long dependsOnTaskId) {
        return Result.success(taskDependencyService.findByDependsOnTaskId(dependsOnTaskId));
    }

    @GetMapping("/task/{taskId}/blocking-count")
    public Result<Integer> countBlockingDependencies(@PathVariable Long taskId) {
        return Result.success(taskDependencyService.countBlockingDependencies(taskId));
    }

    @GetMapping("/task/{taskId}/can-transition/{targetStatusId}")
    public Result<Boolean> canTransitionTo(@PathVariable Long taskId, @PathVariable Long targetStatusId) {
        return Result.success(taskDependencyService.canTransitionTo(taskId, targetStatusId));
    }

    @PostMapping
    public Result<TaskDependency> create(@RequestBody TaskDependency dependency) {
        taskDependencyService.save(dependency);
        return Result.success(dependency);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskDependencyService.removeById(id);
        return Result.success();
    }
}
