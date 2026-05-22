package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprints")
public class SprintController {

    private final SprintMapper sprintMapper;
    private final TaskMapper taskMapper;

    public SprintController(SprintMapper sprintMapper, TaskMapper taskMapper) {
        this.sprintMapper = sprintMapper;
        this.taskMapper = taskMapper;
    }

    @PostMapping
    public Result<Sprint> create(@PathVariable Long projectId, @RequestBody Sprint sprint) {
        sprint.setProjectId(projectId);
        sprintMapper.insert(sprint);
        return Result.success(sprint);
    }

    @GetMapping
    public Result<List<Sprint>> list(@PathVariable Long projectId) {
        return Result.success(sprintMapper.findByProjectId(projectId));
    }

    @GetMapping("/{id}")
    public Result<Sprint> getById(@PathVariable Long id) {
        return Result.success(sprintMapper.findById(id));
    }

    @PutMapping("/{id}")
    public Result<Sprint> update(@PathVariable Long id, @RequestBody Sprint sprint) {
        sprint.setId(id);
        sprintMapper.updateById(sprint);
        return Result.success(sprint);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sprintMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/start")
    public Result<Sprint> startSprint(@PathVariable Long id) {
        Sprint sprint = sprintMapper.findById(id);
        if (sprint == null) {
            return Result.error("Sprint not found");
        }
        sprint.setStatus(2); // ACTIVE
        sprint.setStartDate(LocalDateTime.now());
        sprintMapper.updateById(sprint);
        return Result.success(sprint);
    }

    @PutMapping("/{id}/complete")
    public Result<Sprint> completeSprint(@PathVariable Long id) {
        Sprint sprint = sprintMapper.findById(id);
        if (sprint == null) {
            return Result.error("Sprint not found");
        }
        sprint.setStatus(3); // COMPLETED
        sprint.setEndDate(LocalDateTime.now());
        sprintMapper.updateById(sprint);
        return Result.success(sprint);
    }

    @GetMapping("/{id}/tasks")
    public Result<List<Task>> getSprintTasks(@PathVariable Long id) {
        return Result.success(taskMapper.findBySprintId(id));
    }
}
