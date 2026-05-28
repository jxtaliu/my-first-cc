package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Project;
import com.sme.pm.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目控制器
 * 提供项目管理相关API，包括项目的CRUD、项目成员管理、归档恢复等
 *
 * 用法：
 * - POST /api/projects - 创建新项目
 * - GET /api/projects - 获取项目列表
 * - GET /api/projects/{id} - 获取项目详情
 * - PUT /api/projects/{id} - 更新项目信息
 * - DELETE /api/projects/{id} - 删除项目
 * - POST /api/projects/{id}/archive - 归档项目
 * - POST /api/projects/{id}/restore - 恢复项目
 * - GET /api/projects/{projectId}/members - 获取项目成员
 * - POST /api/projects/{projectId}/members - 添加项目成员
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 创建新项目
     * @param project 项目信息
     * @param userId 当前登录用户ID（作为项目所有者）
     * @return 创建的项目信息
     */
    @PostMapping
    public Result<Project> create(@RequestBody Project project, @CurrentUser Long userId) {
        project.setOwnerId(userId);
        return Result.success(projectService.create(project));
    }

    /**
     * 更新项目信息
     * @param id 项目ID
     * @param project 更新后的项目信息
     * @return 更新后的项目信息
     */
    @PutMapping("/{id}")
    public Result<Project> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return Result.success(projectService.update(project));
    }

    /**
     * 删除项目
     * @param id 项目ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    /**
     * 根据ID获取项目详情
     * @param id 项目ID
     * @return 项目信息
     */
    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    /**
     * 获取项目列表
     * @param status 可选，按状态筛选（ACTIVE, ARCHIVED）
     * @return 项目列表
     */
    @GetMapping
    public Result<List<Project>> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return Result.success(projectService.listByStatus(status));
        }
        return Result.success(projectService.list());
    }

    /**
     * 归档项目
     * @param id 项目ID
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        projectService.archive(id);
        return Result.success();
    }

    /**
     * 恢复项目
     * @param id 项目ID
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        projectService.restore(id);
        return Result.success();
    }

    /**
     * 获取项目成员列表
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    @GetMapping("/{projectId}/members")
    public Result<?> getMembers(@PathVariable String projectId) {
        return Result.success(projectService.getMembers(projectId));
    }

    /**
     * 添加项目成员
     * @param projectId 项目ID
     * @param member 包含userId和roleId的请求体
     * @return 成功返回空结果
     */
    @PostMapping("/{projectId}/members")
    public Result<Void> addMember(@PathVariable String projectId, @RequestBody Map<String, Object> member) {
        Object userIdObj = member.get("userId");
        Long userId = (userIdObj instanceof Number) ? ((Number) userIdObj).longValue() : Long.parseLong(userIdObj.toString());
        String roleId = (String) member.getOrDefault("roleId", "ROLE_DEVELOPER");
        projectService.addMember(projectId, userId, roleId);
        return Result.success();
    }

    /**
     * 更新项目成员角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param member 包含roleId的请求体
     * @return 成功返回空结果
     */
    @PutMapping("/{projectId}/members/{userId}")
    public Result<Void> updateMemberRole(@PathVariable String projectId, @PathVariable Long userId, @RequestBody Map<String, Object> member) {
        String roleId = (String) member.get("roleId");
        projectService.updateMemberRole(projectId, userId, roleId);
        return Result.success();
    }

    /**
     * 移除项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable String projectId, @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
        return Result.success();
    }

    /**
     * 获取项目统计信息
     * @param projectId 项目ID
     * @return 项目统计数据（任务数、成员数等）
     */
    @GetMapping("/{projectId}/stats")
    public Result<Object> getStats(@PathVariable String projectId) {
        return Result.success(projectService.getStats(projectId));
    }
}
