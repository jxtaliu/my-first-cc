package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Project;
import com.sme.pm.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{projectId}/members")
    public Result<?> getMembers(@PathVariable Long projectId) {
        return Result.success(projectService.getMembers(projectId));
    }

    @PostMapping("/{projectId}/members")
    public Result<Void> addMember(@PathVariable Long projectId, @RequestBody Map<String, Object> member) {
        Object userIdObj = member.get("userId");
        Long userId = (userIdObj instanceof Number) ? ((Number) userIdObj).longValue() : Long.parseLong(userIdObj.toString());
        projectService.addMember(projectId, userId);
        return Result.success();
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
        return Result.success();
    }

    @GetMapping("/{projectId}/stats")
    public Result<Object> getStats(@PathVariable Long projectId) {
        return Result.success(projectService.getStats(projectId));
    }
}
