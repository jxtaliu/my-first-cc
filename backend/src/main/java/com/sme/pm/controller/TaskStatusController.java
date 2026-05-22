package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.service.ITaskStatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task-statuses")
public class TaskStatusController {

    private final ITaskStatusService taskStatusService;

    public TaskStatusController(ITaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    @GetMapping("/project/{projectId}")
    public Result<List<TaskStatus>> findByProjectId(@PathVariable Long projectId) {
        return Result.success(taskStatusService.findByProjectId(projectId));
    }

    @GetMapping("/system")
    public Result<List<TaskStatus>> findSystemDefaults() {
        return Result.success(taskStatusService.findSystemDefaults());
    }

    @GetMapping("/project/{projectId}/{code}")
    public Result<TaskStatus> findByCode(@PathVariable Long projectId, @PathVariable String code) {
        TaskStatus status = taskStatusService.findByCode(projectId, code);
        return status != null ? Result.success(status) : Result.error("Status not found");
    }

    @PostMapping
    public Result<TaskStatus> create(@RequestBody TaskStatus status) {
        taskStatusService.save(status);
        return Result.success(status);
    }

    @PutMapping("/{id}")
    public Result<TaskStatus> update(@PathVariable Long id, @RequestBody TaskStatus status) {
        status.setId(id);
        taskStatusService.updateById(status);
        return Result.success(status);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskStatusService.removeById(id);
        return Result.success();
    }
}
