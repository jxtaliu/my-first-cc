package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.service.ITaskAttachmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务附件控制器
 * 提供任务附件管理相关API，包括附件上传、下载、删除等
 *
 * 用法：
 * - GET /api/v1/task-attachments/task/{taskId} - 获取任务的附件列表
 * - GET /api/v1/task-attachments/{id} - 获取附件详情
 * - POST /api/v1/task-attachments - 上传附件
 * - DELETE /api/v1/task-attachments/{id} - 删除附件
 */
@RestController
@RequestMapping("/api/v1/task-attachments")
public class TaskAttachmentController {

    private final ITaskAttachmentService taskAttachmentService;

    public TaskAttachmentController(ITaskAttachmentService taskAttachmentService) {
        this.taskAttachmentService = taskAttachmentService;
    }

    /**
     * 获取任务的附件列表
     * @param taskId 任务ID
     * @return 附件列表
     */
    @GetMapping("/task/{taskId}")
    public Result<List<TaskAttachment>> findByTaskId(@PathVariable Long taskId) {
        return Result.success(taskAttachmentService.findByTaskId(taskId));
    }

    /**
     * 根据ID获取附件详情
     * @param id 附件ID
     * @return 附件信息
     */
    @GetMapping("/{id}")
    public Result<TaskAttachment> findById(@PathVariable Long id) {
        TaskAttachment attachment = taskAttachmentService.findById(id);
        return attachment != null ? Result.success(attachment) : Result.error("Attachment not found");
    }

    /**
     * 上传任务附件
     * @param attachment 附件信息
     * @return 上传后的附件信息
     */
    @PostMapping
    public Result<TaskAttachment> upload(@RequestBody TaskAttachment attachment) {
        taskAttachmentService.uploadAttachment(attachment);
        return Result.success(attachment);
    }

    /**
     * 删除任务附件
     * @param id 附件ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskAttachmentService.deleteAttachment(id);
        return Result.success();
    }
}
