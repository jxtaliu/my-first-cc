package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.service.ITaskStatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务状态控制器
 * 提供任务状态管理相关API，包括状态CRUD、初始化、排序等
 *
 * 用法：
 * - GET /api/v1/task-statuses/project/{projectId} - 获取项目的任务状态列表
 * - GET /api/v1/task-statuses/system - 获取系统默认状态
 * - POST /api/v1/task-statuses - 创建任务状态
 * - PUT /api/v1/task-statuses/{id} - 更新任务状态
 * - DELETE /api/v1/task-statuses/{id} - 删除任务状态
 * - POST /api/v1/task-statuses/init/{projectId} - 从字典初始化项目状态
 * - PUT /api/v1/task-statuses/reorder - 排序任务状态
 */
@RestController
@RequestMapping("/api/v1/task-statuses")
public class TaskStatusController {

    private final ITaskStatusService taskStatusService;

    public TaskStatusController(ITaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    /**
     * 获取项目的任务状态列表
     * @param projectId 项目ID
     * @return 任务状态列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<TaskStatus>> findByProjectId(@PathVariable String projectId) {
        return Result.success(taskStatusService.findByProjectId(projectId));
    }

    /**
     * 获取系统默认任务状态
     * @return 系统默认状态列表
     */
    @GetMapping("/system")
    public Result<List<TaskStatus>> findSystemDefaults() {
        return Result.success(taskStatusService.findSystemDefaults());
    }

    /**
     * 根据项目ID和状态代码获取状态
     * @param projectId 项目ID
     * @param code 状态代码
     * @return 任务状态信息
     */
    @GetMapping("/project/{projectId}/{code}")
    public Result<TaskStatus> findByCode(@PathVariable String projectId, @PathVariable String code) {
        TaskStatus status = taskStatusService.findByCode(projectId, code);
        return status != null ? Result.success(status) : Result.error("Status not found");
    }

    /**
     * 创建任务状态
     * @param status 状态信息
     * @return 创建的状态信息
     */
    @PostMapping
    public Result<TaskStatus> create(@RequestBody TaskStatus status) {
        taskStatusService.save(status);
        return Result.success(status);
    }

    /**
     * 更新任务状态
     * @param id 状态ID
     * @param status 更新后的状态信息
     * @return 更新后的状态信息
     */
    @PutMapping("/{id}")
    public Result<TaskStatus> update(@PathVariable Long id, @RequestBody TaskStatus status) {
        status.setId(id);
        taskStatusService.updateStatus(status);
        return Result.success(status);
    }

    /**
     * 删除任务状态
     * @param id 状态ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskStatusService.removeById(id);
        return Result.success();
    }

    /**
     * 从字典初始化项目任务状态
     * @param projectId 项目ID
     * @return 成功返回空结果
     */
    @PostMapping("/init/{projectId}")
    public Result<Void> initializeFromDict(@PathVariable String projectId) {
        taskStatusService.initializeFromDict(projectId);
        return Result.success();
    }

    /**
     * 排序任务状态
     * @param statusIds 状态ID列表（按排序顺序）
     * @return 成功返回空结果
     */
    @PutMapping("/reorder")
    public Result<Void> reorder(@RequestBody List<Long> statusIds) {
        taskStatusService.reorder(statusIds);
        return Result.success();
    }
}
