package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Sprint;
import com.sme.pm.mapper.SprintMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
public class SprintController {

    private final SprintMapper sprintMapper;

    public SprintController(SprintMapper sprintMapper) {
        this.sprintMapper = sprintMapper;
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
}
