package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.service.ITaskAttachmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task-attachments")
public class TaskAttachmentController {

    private final ITaskAttachmentService taskAttachmentService;

    public TaskAttachmentController(ITaskAttachmentService taskAttachmentService) {
        this.taskAttachmentService = taskAttachmentService;
    }

    @GetMapping("/task/{taskId}")
    public Result<List<TaskAttachment>> findByTaskId(@PathVariable Long taskId) {
        return Result.success(taskAttachmentService.findByTaskId(taskId));
    }

    @GetMapping("/{id}")
    public Result<TaskAttachment> findById(@PathVariable Long id) {
        TaskAttachment attachment = taskAttachmentService.findById(id);
        return attachment != null ? Result.success(attachment) : Result.error("Attachment not found");
    }

    @PostMapping
    public Result<TaskAttachment> upload(@RequestBody TaskAttachment attachment) {
        taskAttachmentService.uploadAttachment(attachment);
        return Result.success(attachment);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskAttachmentService.deleteAttachment(id);
        return Result.success();
    }
}
