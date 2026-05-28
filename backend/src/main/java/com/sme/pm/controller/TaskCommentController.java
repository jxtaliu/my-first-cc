package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.service.ITaskCommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务评论控制器
 * 提供任务评论管理相关API，包括评论CRUD等
 *
 * 用法：
 * - GET /api/v1/task-comments/task/{taskId} - 获取任务的评论列表
 * - GET /api/v1/task-comments/{id} - 获取评论详情
 * - POST /api/v1/task-comments - 创建评论
 * - PUT /api/v1/task-comments/{id} - 更新评论
 * - DELETE /api/v1/task-comments/{id} - 删除评论
 */
@RestController
@RequestMapping("/api/v1/task-comments")
public class TaskCommentController {

    private final ITaskCommentService taskCommentService;

    public TaskCommentController(ITaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    /**
     * 获取任务的评论列表
     * @param taskId 任务ID
     * @return 评论列表
     */
    @GetMapping("/task/{taskId}")
    public Result<List<TaskComment>> findByTaskId(@PathVariable Long taskId) {
        return Result.success(taskCommentService.findByTaskId(taskId));
    }

    /**
     * 获取父评论的所有回复
     * @param parentId 父评论ID
     * @return 回复列表
     */
    @GetMapping("/parent/{parentId}")
    public Result<List<TaskComment>> findByParentId(@PathVariable Long parentId) {
        return Result.success(taskCommentService.findByParentId(parentId));
    }

    /**
     * 根据ID获取评论详情
     * @param id 评论ID
     * @return 评论信息
     */
    @GetMapping("/{id}")
    public Result<TaskComment> findById(@PathVariable Long id) {
        TaskComment comment = taskCommentService.findById(id);
        return comment != null ? Result.success(comment) : Result.error("Comment not found");
    }

    /**
     * 创建评论
     * @param comment 评论内容
     * @return 创建的评论信息
     */
    @PostMapping
    public Result<TaskComment> create(@RequestBody TaskComment comment) {
        taskCommentService.save(comment);
        return Result.success(comment);
    }

    /**
     * 更新评论
     * @param id 评论ID
     * @param comment 更新后的评论内容
     * @return 更新后的评论信息
     */
    @PutMapping("/{id}")
    public Result<TaskComment> update(@PathVariable Long id, @RequestBody TaskComment comment) {
        comment.setId(id);
        taskCommentService.updateById(comment);
        return Result.success(comment);
    }

    /**
     * 删除评论
     * @param id 评论ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskCommentService.removeById(id);
        return Result.success();
    }
}
