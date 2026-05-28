package com.sme.pm.controller;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 * 提供任务管理相关API，包括任务CRUD、状态管理、任务移动、评论、附件、依赖关系等
 *
 * 用法：
 * - POST /api/v1/tasks - 创建任务
 * - GET /api/v1/tasks - 查询任务列表（按sprintId/projectId/assigneeId筛选）
 * - GET /api/v1/tasks/{id} - 获取任务详情
 * - PUT /api/v1/tasks/{id} - 更新任务
 * - DELETE /api/v1/tasks/{id} - 删除任务
 * - PUT /api/v1/tasks/{id}/move - 移动任务到其他冲刺/状态
 * - PUT /api/v1/tasks/batch-move - 批量移动任务
 * - GET /api/v1/tasks/{id}/comments - 获取任务评论
 * - POST /api/v1/tasks/{id}/comments - 添加任务评论
 * - GET /api/v1/tasks/{id}/attachments - 获取任务附件
 * - GET /api/v1/tasks/{id}/dependencies - 获取任务依赖
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequireProjectRole(memberOnly = true)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 创建新任务
     * @param task 任务信息
     * @param userId 当前登录用户ID（作为任务负责人）
     * @return 创建的任务信息
     */
    @PostMapping
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "create_task")
    public Result<Task> create(@RequestBody Task task, @CurrentUser Long userId) {
        task.setAssigneeId(userId);
        return Result.success(taskService.create(task));
    }

    /**
     * 查询任务列表
     * @param sprintId 可选，按冲刺筛选
     * @param projectId 可选，按项目筛选
     * @param assigneeId 可选，按负责人筛选
     * @return 任务列表
     */
    @GetMapping
    public Result<List<Task>> list(@RequestParam(required = false) Long sprintId,
                                   @RequestParam(required = false) String projectId,
                                   @RequestParam(required = false) Long assigneeId) {
        if (sprintId != null) {
            return Result.success(taskService.listBySprint(sprintId));
        }
        if (projectId != null) {
            return Result.success(taskService.listByProject(projectId));
        }
        if (assigneeId != null) {
            return Result.success(taskService.listByAssignee(assigneeId));
        }
        return Result.error("Must provide sprintId, projectId, or assigneeId");
    }

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务信息
     */
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    /**
     * 更新任务信息
     * @param id 任务ID
     * @param task 更新后的任务信息
     * @return 更新后的任务信息
     */
    @PutMapping("/{id}")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "update_task")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.update(task));
    }

    /**
     * 删除任务
     * @param id 任务ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    @RequireProjectRole(value = {"PROJECT_OWNER"}, operation = "delete_task")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }

    /**
     * 移动任务（到其他冲刺或状态）
     * @param id 任务ID
     * @param task 包含sprintId和status的目标信息
     * @return 移动后的任务信息
     */
    @PutMapping("/{id}/move")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "move_task")
    public Result<Task> move(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.move(task));
    }

    /**
     * 批量移动任务
     * @param request 包含taskIds、targetStatus、sprintId的请求体
     * @param projectId 项目ID
     * @return 移动结果（成功数、失败数、错误列表）
     */
    @PutMapping("/batch-move")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "move_task")
    public Result<Map<String, Object>> batchMove(@RequestBody Map<String, Object> request,
                                                  @RequestParam String projectId) {
        @SuppressWarnings("unchecked")
        List<Number> taskIdNumbers = (List<Number>) request.get("taskIds");
        List<Long> taskIds = taskIdNumbers.stream().map(Number::longValue).toList();
        String targetStatus = (String) request.get("targetStatus");
        Long sprintId = request.get("sprintId") != null ? ((Number) request.get("sprintId")).longValue() : null;
        return Result.success(taskService.batchMove(taskIds, targetStatus, sprintId));
    }

    /**
     * 分配任务给用户
     * @param id 任务ID
     * @param userId 目标用户ID
     * @return 成功返回空结果
     */
    @PutMapping("/{id}/assign")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "assign_task")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long userId) {
        taskService.assign(id, userId);
        return Result.success();
    }

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param statusCode 目标状态代码（如TODO, IN_PROGRESS, DONE）
     * @return 更新后的任务信息
     */
    @PutMapping("/{id}/status")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "update_task_status")
    public Result<Task> updateStatus(@PathVariable Long id, @RequestParam String statusCode) {
        return Result.success(taskService.updateStatus(id, statusCode));
    }

    /**
     * 检查任务是否可以转换到目标状态
     * @param id 任务ID
     * @param targetStatusCode 目标状态代码
     * @return 是否可以转换
     */
    @GetMapping("/{id}/can-transition/{targetStatusCode}")
    public Result<Boolean> canTransitionTo(@PathVariable Long id, @PathVariable String targetStatusCode) {
        return Result.success(taskService.canTransitionTo(id, targetStatusCode));
    }

    /**
     * 获取任务评论列表
     * @param id 任务ID
     * @return 评论列表
     */
    @GetMapping("/{id}/comments")
    public Result<List<TaskComment>> getComments(@PathVariable Long id) {
        return Result.success(taskService.getComments(id));
    }

    /**
     * 添加任务评论
     * @param id 任务ID
     * @param comment 评论内容
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/comments")
    public Result<Void> addComment(@PathVariable Long id, @RequestBody TaskComment comment) {
        taskService.addComment(id, comment);
        return Result.success();
    }

    /**
     * 获取任务附件列表
     * @param id 任务ID
     * @return 附件列表
     */
    @GetMapping("/{id}/attachments")
    public Result<List<TaskAttachment>> getAttachments(@PathVariable Long id) {
        return Result.success(taskService.getAttachments(id));
    }

    /**
     * 添加任务附件
     * @param id 任务ID
     * @param attachment 附件信息
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/attachments")
    public Result<Void> addAttachment(@PathVariable Long id, @RequestBody TaskAttachment attachment) {
        taskService.addAttachment(id, attachment);
        return Result.success();
    }

    /**
     * 删除任务附件
     * @param attachmentId 附件ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public Result<Void> deleteAttachment(@PathVariable Long attachmentId) {
        taskService.deleteAttachment(attachmentId);
        return Result.success();
    }

    /**
     * 获取任务依赖列表（此任务依赖的其他任务）
     * @param id 任务ID
     * @return 依赖列表
     */
    @GetMapping("/{id}/dependencies")
    public Result<List<TaskDependency>> getDependencies(@PathVariable Long id) {
        return Result.success(taskService.getDependencies(id));
    }

    /**
     * 获取阻塞此任务的任务列表（依赖此任务的任务）
     * @param id 任务ID
     * @return 阻塞依赖列表
     */
    @GetMapping("/{id}/blocking")
    public Result<List<TaskDependency>> getBlockingDependencies(@PathVariable Long id) {
        return Result.success(taskService.getBlockingDependencies(id));
    }

    /**
     * 统计阻塞此任务的任务数量
     * @param id 任务ID
     * @return 阻塞数量
     */
    @GetMapping("/{id}/blocking-count")
    public Result<Integer> countBlockingDependencies(@PathVariable Long id) {
        return Result.success(taskService.countBlockingDependencies(id));
    }

    /**
     * 添加任务依赖关系
     * @param id 任务ID
     * @param dependency 依赖信息（dependsOnTaskId表示依赖的任务ID）
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/dependencies")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER", "DEVELOPER"}, operation = "add_dependency")
    public Result<Void> addDependency(@PathVariable Long id, @RequestBody TaskDependency dependency) {
        taskService.addDependency(id, dependency);
        return Result.success();
    }

    /**
     * 删除任务依赖关系
     * @param dependencyId 依赖关系ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/dependencies/{dependencyId}")
    @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"}, operation = "remove_dependency")
    public Result<Void> removeDependency(@PathVariable Long dependencyId) {
        taskService.removeDependency(dependencyId);
        return Result.success();
    }

    /**
     * 按状态ID统计任务数量
     * @param statusId 状态ID
     * @return 任务数量
     */
    @GetMapping("/count")
    public Result<Long> countByStatus(@RequestParam Long statusId) {
        long count = taskService.countByStatusId(statusId.intValue());
        return Result.success(count);
    }
}
