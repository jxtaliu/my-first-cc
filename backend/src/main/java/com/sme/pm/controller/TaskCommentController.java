package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.service.ITaskCommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task-comments")
public class TaskCommentController {

    private final ITaskCommentService taskCommentService;

    public TaskCommentController(ITaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    @GetMapping("/task/{taskId}")
    public Result<List<TaskComment>> findByTaskId(@PathVariable Long taskId) {
        return Result.success(taskCommentService.findByTaskId(taskId));
    }

    @GetMapping("/parent/{parentId}")
    public Result<List<TaskComment>> findByParentId(@PathVariable Long parentId) {
        return Result.success(taskCommentService.findByParentId(parentId));
    }

    @GetMapping("/{id}")
    public Result<TaskComment> findById(@PathVariable Long id) {
        TaskComment comment = taskCommentService.findById(id);
        return comment != null ? Result.success(comment) : Result.error("Comment not found");
    }

    @PostMapping
    public Result<TaskComment> create(@RequestBody TaskComment comment) {
        taskCommentService.save(comment);
        return Result.success(comment);
    }

    @PutMapping("/{id}")
    public Result<TaskComment> update(@PathVariable Long id, @RequestBody TaskComment comment) {
        comment.setId(id);
        taskCommentService.updateById(comment);
        return Result.success(comment);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskCommentService.removeById(id);
        return Result.success();
    }
}
