package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.ProjectRole;
import com.sme.pm.service.IProjectRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目角色控制器
 * 提供项目级角色管理相关API，包括项目成员角色分配、查询等
 *
 * 用法：
 * - GET /api/v1/project-roles/project/{projectId} - 获取项目的所有角色成员
 * - GET /api/v1/project-roles/user/{userId} - 获取用户在所有项目的角色
 * - GET /api/v1/project-roles/project/{projectId}/user/{userId} - 获取用户在特定项目的角色
 * - POST /api/v1/project-roles - 分配项目角色
 * - DELETE /api/v1/project-roles/project/{projectId}/user/{userId} - 移除项目角色
 */
@RestController
@RequestMapping("/api/v1/project-roles")
public class ProjectRoleController {

    private final IProjectRoleService projectRoleService;

    public ProjectRoleController(IProjectRoleService projectRoleService) {
        this.projectRoleService = projectRoleService;
    }

    /**
     * 获取项目的所有角色成员
     * @param projectId 项目ID
     * @return 项目角色成员列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<ProjectRole>> findByProjectId(@PathVariable String projectId) {
        return Result.success(projectRoleService.findByProjectId(projectId));
    }

    /**
     * 获取用户在所有项目的角色
     * @param userId 用户ID
     * @return 用户在所有项目的角色列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<ProjectRole>> findByUserId(@PathVariable Long userId) {
        return Result.success(projectRoleService.findByUserId(userId));
    }

    /**
     * 获取用户在特定项目的角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 项目角色信息
     */
    @GetMapping("/project/{projectId}/user/{userId}")
    public Result<ProjectRole> findByProjectAndUser(@PathVariable String projectId, @PathVariable Long userId) {
        ProjectRole role = projectRoleService.findByProjectAndUser(projectId, userId);
        return role != null ? Result.success(role) : Result.error("Role not found");
    }

    /**
     * 获取项目下特定角色的所有成员
     * @param projectId 项目ID
     * @param role 角色名称
     * @return 角色成员列表
     */
    @GetMapping("/project/{projectId}/role/{role}")
    public Result<List<ProjectRole>> findByProjectAndRole(@PathVariable String projectId, @PathVariable String role) {
        return Result.success(projectRoleService.findByProjectAndRole(projectId, role));
    }

    /**
     * 为用户分配项目角色
     * @param projectRole 项目角色信息（包含projectId、userId、role）
     * @return 成功返回空结果
     */
    @PostMapping
    public Result<Void> assignRole(@RequestBody ProjectRole projectRole) {
        projectRoleService.assignRole(projectRole.getProjectId(), projectRole.getUserId(), projectRole.getRole());
        return Result.success();
    }

    /**
     * 移除用户的项目角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/project/{projectId}/user/{userId}")
    public Result<Void> removeRole(@PathVariable String projectId, @PathVariable Long userId) {
        projectRoleService.removeRole(projectId, userId);
        return Result.success();
    }
}
