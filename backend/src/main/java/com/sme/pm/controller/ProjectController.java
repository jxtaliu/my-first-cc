package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Project;
import com.sme.pm.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Result<Project> create(@RequestBody Project project, @CurrentUser Long userId) {
        project.setOwnerId(userId);
        return Result.success(projectService.create(project));
    }

    @PutMapping("/{id}")
    public Result<Project> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return Result.success(projectService.update(project));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @GetMapping
    public Result<List<Project>> list() {
        return Result.success(projectService.list());
    }

    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        projectService.archive(id);
        return Result.success();
    }

    @PostMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        projectService.restore(id);
        return Result.success();
    }

    @PostMapping("/{id}/members/{userId}")
    public Result<Void> addMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.addMember(id, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeMember(id, userId);
        return Result.success();
    }
}
