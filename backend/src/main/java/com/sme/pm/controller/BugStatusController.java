package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.Result;
import com.sme.pm.entity.BugStatus;
import com.sme.pm.service.IBugStatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Bug状态控制器
 * 提供Bug状态管理相关API，包括Bug状态列表查询、状态转换检查、初始化等
 *
 * 用法：
 * - GET /api/v1/bug-statuses/project/{projectId} - 获取项目的Bug状态列表
 * - GET /api/v1/bug-statuses/project/{projectId}/transitions/{fromStatus} - 获取允许的状态转换
 * - GET /api/v1/bug-statuses/project/{projectId}/can-transition - 检查是否可以转换
 * - POST /api/v1/bug-statuses/init/{projectId} - 初始化项目Bug状态
 */
@RestController
@RequestMapping("/api/v1/bug-statuses")
public class BugStatusController {

    private final IBugStatusService bugStatusService;

    public BugStatusController(IBugStatusService bugStatusService) {
        this.bugStatusService = bugStatusService;
    }

    /**
     * 获取项目的Bug状态列表
     * @param projectId 项目ID
     * @return Bug状态列表
     */
    @GetMapping("/project/{projectId}")
    @RequireProjectRole(memberOnly = true)
    public Result<List<BugStatus>> getByProjectId(@PathVariable String projectId) {
        return Result.success(bugStatusService.getByProjectId(projectId));
    }

    /**
     * 获取Bug允许的状态转换列表
     * @param projectId 项目ID
     * @param fromStatus 当前状态
     * @return 可以转换到的状态列表
     */
    @GetMapping("/project/{projectId}/transitions/{fromStatus}")
    @RequireProjectRole(memberOnly = true)
    public Result<List<String>> getAllowedTransitions(
            @PathVariable String projectId,
            @PathVariable String fromStatus) {
        return Result.success(bugStatusService.getAllowedTransitions(projectId, fromStatus));
    }

    /**
     * 检查Bug状态是否可以转换
     * @param projectId 项目ID
     * @param fromStatus 当前状态
     * @param toStatus 目标状态
     * @return 是否可以转换
     */
    @GetMapping("/project/{projectId}/can-transition")
    @RequireProjectRole(memberOnly = true)
    public Result<Boolean> canTransition(
            @PathVariable String projectId,
            @RequestParam String fromStatus,
            @RequestParam String toStatus) {
        return Result.success(bugStatusService.canTransition(projectId, fromStatus, toStatus));
    }

    /**
     * 初始化项目的Bug状态
     * @param projectId 项目ID
     * @return 成功返回空结果
     */
    @PostMapping("/init/{projectId}")
    @RequireProjectRole(value = {"PROJECT_OWNER"}, operation = "init_bug_statuses")
    public Result<Void> initializeForProject(@PathVariable String projectId) {
        bugStatusService.initializeForProject(projectId);
        return Result.success();
    }
}
