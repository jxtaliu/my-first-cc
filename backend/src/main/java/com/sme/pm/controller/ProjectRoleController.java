package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.ProjectRole;
import com.sme.pm.service.IProjectRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project-roles")
public class ProjectRoleController {

    private final IProjectRoleService projectRoleService;

    public ProjectRoleController(IProjectRoleService projectRoleService) {
        this.projectRoleService = projectRoleService;
    }

    @GetMapping("/project/{projectId}")
    public Result<List<ProjectRole>> findByProjectId(@PathVariable Long projectId) {
        return Result.success(projectRoleService.findByProjectId(projectId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<ProjectRole>> findByUserId(@PathVariable Long userId) {
        return Result.success(projectRoleService.findByUserId(userId));
    }

    @GetMapping("/project/{projectId}/user/{userId}")
    public Result<ProjectRole> findByProjectAndUser(@PathVariable Long projectId, @PathVariable Long userId) {
        ProjectRole role = projectRoleService.findByProjectAndUser(projectId, userId);
        return role != null ? Result.success(role) : Result.error("Role not found");
    }

    @GetMapping("/project/{projectId}/role/{role}")
    public Result<List<ProjectRole>> findByProjectAndRole(@PathVariable Long projectId, @PathVariable String role) {
        return Result.success(projectRoleService.findByProjectAndRole(projectId, role));
    }

    @PostMapping
    public Result<Void> assignRole(@RequestBody ProjectRole projectRole) {
        projectRoleService.assignRole(projectRole.getProjectId(), projectRole.getUserId(), projectRole.getRole());
        return Result.success();
    }

    @DeleteMapping("/project/{projectId}/user/{userId}")
    public Result<Void> removeRole(@PathVariable Long projectId, @PathVariable Long userId) {
        projectRoleService.removeRole(projectId, userId);
        return Result.success();
    }
}
