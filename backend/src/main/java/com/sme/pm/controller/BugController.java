package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.entity.BugStatus;
import com.sme.pm.service.IBugStatusService;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Bug控制器
 * 提供Bug管理相关API，包括Bug列表、创建、状态管理等
 *
 * 用法：
 * - GET /api/v1/tasks/bugs - 获取项目的Bug列表
 * - POST /api/v1/tasks/bugs - 创建Bug
 * - GET /api/v1/tasks/bugs/statuses/{projectId} - 获取Bug状态列表
 * - GET /api/v1/tasks/bugs/transitions/{projectId}/{fromStatus} - 获取允许的状态转换
 * - PUT /api/v1/tasks/bugs/{id}/status - 更新Bug状态
 */
@RestController
@RequestMapping("/api/v1/tasks/bugs")
public class BugController {

    private final TaskService taskService;
    private final IBugStatusService bugStatusService;

    public BugController(TaskService taskService, IBugStatusService bugStatusService) {
        this.taskService = taskService;
        this.bugStatusService = bugStatusService;
    }

    /**
     * 获取项目的Bug列表
     * @param projectId 项目ID
     * @return Bug任务列表
     */
    @GetMapping
    @RequireProjectRole(memberOnly = true)
    public Result<List<Task>> getBugs(@RequestParam String projectId) {
        return Result.success(taskService.getBugs(projectId));
    }

    /**
     * 创建Bug
     * @param bug Bug信息
     * @param userId 当前登录用户ID（作为Bug负责人）
     * @return 创建的Bug信息
     */
    @PostMapping
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "create_bug")
    public Result<Task> createBug(@RequestBody Task bug, @CurrentUser Long userId) {
        bug.setType("BUG");
        bug.setAssigneeId(userId);
        return Result.success(taskService.create(bug));
    }

    /**
     * 获取项目的Bug状态列表
     * @param projectId 项目ID
     * @return Bug状态列表
     */
    @GetMapping("/statuses/{projectId}")
    public Result<List<BugStatus>> getBugStatuses(@PathVariable String projectId) {
        return Result.success(bugStatusService.getByProjectId(projectId));
    }

    /**
     * 获取Bug允许的状态转换列表
     * @param projectId 项目ID
     * @param fromStatus 当前状态
     * @return 可以转换到的状态列表
     */
    @GetMapping("/transitions/{projectId}/{fromStatus}")
    public Result<List<String>> getAllowedTransitions(
            @PathVariable String projectId,
            @PathVariable String fromStatus) {
        return Result.success(bugStatusService.getAllowedTransitions(projectId, fromStatus));
    }

    /**
     * 更新Bug状态
     * @param id Bug任务ID
     * @param bugStatusId 目标Bug状态ID
     * @return 成功返回空结果
     */
    @PutMapping("/{id}/status")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "update_bug_status")
    public Result<Void> updateBugStatus(
            @PathVariable Long id,
            @RequestParam Long bugStatusId) {
        taskService.updateBugStatus(id, bugStatusId);
        return Result.success();
    }
}
