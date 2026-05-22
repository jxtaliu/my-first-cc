package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.mapper.TaskAttachmentMapper;
import com.sme.pm.service.ITaskAttachmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskAttachmentServiceImpl extends ServiceImpl<TaskAttachmentMapper, TaskAttachment> implements ITaskAttachmentService {

    @Override
    public List<TaskAttachment> findByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAttachment::getTaskId, taskId)
               .eq(TaskAttachment::getDeleted, 0)
               .orderByDesc(TaskAttachment::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public TaskAttachment findById(Long id) {
        LambdaQueryWrapper<TaskAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAttachment::getId, id)
               .eq(TaskAttachment::getDeleted, 0);
        return getOne(wrapper);
    }

    @Override
    public void uploadAttachment(TaskAttachment attachment) {
        save(attachment);
    }

    @Override
    public void deleteAttachment(Long id) {
        removeById(id);
    }
}
