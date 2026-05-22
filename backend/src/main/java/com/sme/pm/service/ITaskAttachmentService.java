package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.TaskAttachment;

import java.util.List;

public interface ITaskAttachmentService extends IService<TaskAttachment> {
    List<TaskAttachment> findByTaskId(Long taskId);
    TaskAttachment findById(Long id);
    void uploadAttachment(TaskAttachment attachment);
    void deleteAttachment(Long id);
}
