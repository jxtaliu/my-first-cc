package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.ISprintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 冲刺控制器
 * 提供冲刺（Sprint）管理相关API，包括创建、启动、完成冲刺以及任务管理
 *
 * 用法：
 * - POST /api/v1/projects/{projectId}/sprints - 创建冲刺
 * - GET /api/v1/projects/{projectId}/sprints - 获取项目下的冲刺列表
 * - PUT /api/v1/projects/{projectId}/sprints/{id}/start - 启动冲刺
 * - PUT /api/v1/projects/{projectId}/sprints/{id}/complete - 完成冲刺
 * - GET /api/v1/projects/{projectId}/sprints/backlog - 获取待办任务列表
 * - POST /api/v1/projects/{projectId}/sprints/{sprintId}/tasks/batch - 批量添加任务到冲刺
 * - DELETE /api/v1/projects/{projectId}/sprints/{sprintId}/tasks/batch - 批量从冲刺移除任务
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprints")
public class SprintController {

    private final SprintMapper sprintMapper;
    private final TaskMapper taskMapper;
    private final ISprintService sprintService;

    public SprintController(SprintMapper sprintMapper, TaskMapper taskMapper, ISprintService sprintService) {
        this.sprintMapper = sprintMapper;
        this.taskMapper = taskMapper;
        this.sprintService = sprintService;
    }

    /**
     * 创建新冲刺
     * @param projectId 项目ID
     * @param sprint 冲刺信息
     * @return 创建的冲刺信息
     */
    @PostMapping
    public Result<Sprint> create(@PathVariable String projectId, @RequestBody Sprint sprint) {
        sprint.setProjectId(projectId);
        sprintService.create(sprint);
        return Result.success(sprint);
    }

    /**
     * 获取项目下的冲刺列表
     * @param projectId 项目ID
     * @return 冲刺列表
     */
    @GetMapping
    public Result<List<Sprint>> list(@PathVariable String projectId) {
        return Result.success(sprintMapper.findByProjectId(projectId));
    }

    /**
     * 根据ID获取冲刺详情
     * @param id 冲刺ID
     * @return 冲刺信息
     */
    @GetMapping("/{id}")
    public Result<Sprint> getById(@PathVariable Long id) {
        return Result.success(sprintMapper.findById(id));
    }

    /**
     * 更新冲刺信息
     * @param id 冲刺ID
     * @param sprint 更新后的冲刺信息
     * @return 更新后的冲刺信息
     */
    @PutMapping("/{id}")
    public Result<Sprint> update(@PathVariable Long id, @RequestBody Sprint sprint) {
        sprint.setId(id);
        sprintService.update(sprint);
        return Result.success(sprint);
    }

    /**
     * 删除冲刺
     * @param id 冲刺ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sprintService.delete(id);
        return Result.success();
    }

    /**
     * 启动冲刺
     * @param id 冲刺ID
     * @return 启动后的冲刺信息
     */
    @PutMapping("/{id}/start")
    public Result<Sprint> startSprint(@PathVariable Long id) {
        return Result.success(sprintService.startSprint(id));
    }

    /**
     * 完成冲刺
     * @param id 冲刺ID
     * @return 完成后的冲刺信息
     */
    @PutMapping("/{id}/complete")
    public Result<Sprint> completeSprint(@PathVariable Long id) {
        return Result.success(sprintService.completeSprint(id));
    }

    /**
     * 获取冲刺下的任务列表
     * @param id 冲刺ID
     * @return 任务列表
     */
    @GetMapping("/{id}/tasks")
    public Result<List<Task>> getSprintTasks(@PathVariable Long id) {
        return Result.success(taskMapper.findBySprintId(id));
    }

    /**
     * 获取冲刺统计信息
     * @param sprintId 冲刺ID
     * @return 统计信息（总任务数、完成数、剩余数、velocity）
     */
    @GetMapping("/{sprintId}/stats")
    public Result<Map<String, Object>> getSprintStats(@PathVariable Long sprintId) {
        return Result.success(sprintService.getSprintStats(sprintId));
    }

    /**
     * 计算冲刺velocity（已完成任务的预估工时总和）
     * @param sprintId 冲刺ID
     * @return velocity值
     */
    @GetMapping("/{sprintId}/velocity")
    public Result<Integer> calculateVelocity(@PathVariable Long sprintId) {
        return Result.success(sprintService.calculateVelocity(sprintId));
    }

    /**
     * 获取项目的待办任务列表（未分配到任何冲刺的任务）
     * @param projectId 项目ID
     * @return 待办任务列表
     */
    @GetMapping("/backlog")
    public Result<List<Task>> getBacklogTasks(@PathVariable String projectId) {
        return Result.success(sprintService.getBacklogTasks(projectId));
    }

    /**
     * 添加单个任务到冲刺
     * @param sprintId 冲刺ID
     * @param taskId 任务ID
     * @return 成功返回空结果
     */
    @PostMapping("/{sprintId}/tasks/{taskId}")
    public Result<Void> addTaskToSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        sprintService.addTaskToSprint(sprintId, taskId);
        return Result.success();
    }

    /**
     * 从冲刺移除单个任务
     * @param sprintId 冲刺ID
     * @param taskId 任务ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{sprintId}/tasks/{taskId}")
    public Result<Void> removeTaskFromSprint(@PathVariable Long sprintId, @PathVariable Long taskId) {
        sprintService.removeTaskFromSprint(taskId);
        return Result.success();
    }

    /**
     * 批量添加任务到冲刺
     * @param sprintId 冲刺ID
     * @param body 包含taskIds列表的请求体
     * @return 移动的任务数量
     */
    @PostMapping("/{sprintId}/tasks/batch")
    public Result<Map<String, Integer>> batchAddTasks(
        @PathVariable Long sprintId,
        @RequestBody Map<String, List<Long>> body) {
        List<Long> taskIds = body.get("taskIds");
        int count = sprintService.batchAddTasks(sprintId, taskIds);
        return Result.success(Map.of("movedCount", count));
    }

    /**
     * 批量从冲刺移除任务
     * @param sprintId 冲刺ID
     * @param body 包含taskIds列表的请求体
     * @return 移动的任务数量
     */
    @DeleteMapping("/{sprintId}/tasks/batch")
    public Result<Map<String, Integer>> batchRemoveTasks(
        @PathVariable Long sprintId,
        @RequestBody Map<String, List<Long>> body) {
        List<Long> taskIds = body.get("taskIds");
        int count = sprintService.batchRemoveTasks(sprintId, taskIds);
        return Result.success(Map.of("movedCount", count));
    }
}
