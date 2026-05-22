package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.TaskComment;

import java.util.List;

public interface ITaskCommentService extends IService<TaskComment> {
    List<TaskComment> findByTaskId(Long taskId);
    List<TaskComment> findByParentId(Long parentId);
    TaskComment findById(Long id);
}
