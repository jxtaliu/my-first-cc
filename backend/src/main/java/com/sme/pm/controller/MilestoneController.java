package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Milestone;
import com.sme.pm.service.IMilestoneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/milestones")
public class MilestoneController {

    private final IMilestoneService milestoneService;

    public MilestoneController(IMilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping
    public Result<List<Milestone>> findAll() {
        return Result.success(milestoneService.findAll());
    }

    @GetMapping("/cross-project")
    public Result<List<Milestone>> findCrossProject() {
        return Result.success(milestoneService.findCrossProject());
    }

    @GetMapping("/project/{projectId}")
    public Result<List<Milestone>> findByProjectId(@PathVariable Long projectId) {
        return Result.success(milestoneService.findByProjectId(projectId));
    }

    @GetMapping("/due-soon")
    public Result<List<Milestone>> findDueSoon(@RequestParam(defaultValue = "7") int days) {
        return Result.success(milestoneService.findDueSoon(days));
    }

    @GetMapping("/{id}")
    public Result<Milestone> findById(@PathVariable Long id) {
        Milestone milestone = milestoneService.getById(id);
        return milestone != null ? Result.success(milestone) : Result.error("Milestone not found");
    }

    @PostMapping
    public Result<Milestone> create(@RequestBody Milestone milestone) {
        milestoneService.save(milestone);
        return Result.success(milestone);
    }

    @PutMapping("/{id}")
    public Result<Milestone> update(@PathVariable Long id, @RequestBody Milestone milestone) {
        milestone.setId(id);
        milestoneService.updateById(milestone);
        return Result.success(milestone);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        milestoneService.removeById(id);
        return Result.success();
    }
}
