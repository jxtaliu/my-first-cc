package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.service.ITaskDependencyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务依赖控制器
 * 提供任务依赖关系管理相关API，包括依赖的增删查改、状态转换检查等
 *
 * 用法：
 * - GET /api/v1/task-dependencies/task/{taskId} - 获取任务的依赖列表
 * - GET /api/v1/task-dependencies/depends-on/{dependsOnTaskId} - 获取依赖某任务的所有任务
 * - GET /api/v1/task-dependencies/task/{taskId}/blocking-count - 统计阻塞任务的数量
 * - POST /api/v1/task-dependencies - 创建依赖关系
 * - DELETE /api/v1/task-dependencies/{id} - 删除依赖关系
 */
@RestController
@RequestMapping("/api/v1/task-dependencies")
public class TaskDependencyController {

    private final ITaskDependencyService taskDependencyService;

    public TaskDependencyController(ITaskDependencyService taskDependencyService) {
        this.taskDependencyService = taskDependencyService;
    }

    /**
     * 获取任务的依赖列表（此任务依赖的其他任务）
     * @param taskId 任务ID
     * @return 依赖列表
     */
    @GetMapping("/task/{taskId}")
    public Result<List<TaskDependency>> findByTaskId(@PathVariable Long taskId) {
        return Result.success(taskDependencyService.findByTaskId(taskId));
    }

    /**
     * 获取依赖某任务的所有任务（阻塞列表）
     * @param dependsOnTaskId 被依赖的任务ID
     * @return 依赖该任务的所有任务列表
     */
    @GetMapping("/depends-on/{dependsOnTaskId}")
    public Result<List<TaskDependency>> findByDependsOnTaskId(@PathVariable Long dependsOnTaskId) {
        return Result.success(taskDependencyService.findByDependsOnTaskId(dependsOnTaskId));
    }

    /**
     * 统计阻塞此任务的任务数量
     * @param taskId 任务ID
     * @return 阻塞数量
     */
    @GetMapping("/task/{taskId}/blocking-count")
    public Result<Integer> countBlockingDependencies(@PathVariable Long taskId) {
        return Result.success(taskDependencyService.countBlockingDependencies(taskId));
    }

    /**
     * 检查任务是否可以转换到目标状态（考虑依赖关系）
     * @param taskId 任务ID
     * @param targetStatusCode 目标状态代码
     * @return 是否可以转换
     */
    @GetMapping("/task/{taskId}/can-transition/{targetStatusCode}")
    public Result<Boolean> canTransitionTo(@PathVariable Long taskId, @PathVariable String targetStatusCode) {
        return Result.success(taskDependencyService.canTransitionTo(taskId, targetStatusCode));
    }

    /**
     * 创建任务依赖关系
     * @param dependency 依赖信息（包含taskId和dependsOnTaskId）
     * @return 创建的依赖关系
     */
    @PostMapping
    public Result<TaskDependency> create(@RequestBody TaskDependency dependency) {
        taskDependencyService.save(dependency);
        return Result.success(dependency);
    }

    /**
     * 删除任务依赖关系
     * @param id 依赖关系ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskDependencyService.removeById(id);
        return Result.success();
    }
}
