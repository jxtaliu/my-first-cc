package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.ISprintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprints")
public class SprintController {

    private final SprintMapper sprintMapper;
    private final TaskMapper taskMapper;
    private final ISprintService sprintService;

    public SprintController(SprintMapper sprintMapper, TaskMapper taskMapper, ISprintService sprintService) {
        this.sprintMapper = sprintMapper;
        this.taskMapper = taskMapper;
        this.sprintService = sprintService;
    }

    @PostMapping
    public Result<Sprint> create(@PathVariable String projectId, @RequestBody Sprint sprint) {
        sprint.setProjectId(projectId);
        sprintService.create(sprint);
        return Result.success(sprint);
    }

    @GetMapping
    public Result<List<Sprint>> list(@PathVariable String projectId) {
        return Result.success(sprintMapper.findByProjectId(projectId));
    }

    @GetMapping("/{id}")
    public Result<Sprint> getById(@PathVariable Long id) {
        return Result.success(sprintMapper.findById(id));
    }

    @PutMapping("/{id}")
    public Result<Sprint> update(@PathVariable Long id, @RequestBody Sprint sprint) {
        sprint.setId(id);
        sprintService.update(sprint);
        return Result.success(sprint);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sprintService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/start")
    public Result<Sprint> startSprint(@PathVariable Long id) {
        return Result.success(sprintService.startSprint(id));
    }

    @PutMapping("/{id}/complete")
    public Result<Sprint> completeSprint(@PathVariable Long id) {
        return Result.success(sprintService.completeSprint(id));
    }

    @GetMapping("/{id}/tasks")
    public Result<List<Task>> getSprintTasks(@PathVariable Long id) {
        return Result.success(taskMapper.findBySprintId(id));
    }

    @GetMapping("/{sprintId}/stats")
    public Result<Map<String, Object>> getSprintStats(@PathVariable Long sprintId) {
        return Result.success(sprintService.getSprintStats(sprintId));
    }

    @GetMapping("/{sprintId}/velocity")
    public Result<Integer> calculateVelocity(@PathVariable Long sprintId) {
        return Result.success(sprintService.calculateVelocity(sprintId));
    }

    @GetMapping("/backlog")
    public Result<List<Task>> getBacklogTasks(@PathVariable String projectId) {
        return Result.success(sprintService.getBacklogTasks(projectId));
    }

    @PostMapping("/{sprintId}/tasks/{taskId}")
    public Result<Void> addTaskToSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        sprintService.addTaskToSprint(sprintId, taskId);
        return Result.success();
    }

    @DeleteMapping("/{sprintId}/tasks/{taskId}")
    public Result<Void> removeTaskFromSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        sprintService.removeTaskFromSprint(taskId);
        return Result.success();
    }
}
