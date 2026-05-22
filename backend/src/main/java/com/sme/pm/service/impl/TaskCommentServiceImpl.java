package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.mapper.TaskCommentMapper;
import com.sme.pm.service.ITaskCommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskComment> implements ITaskCommentService {

    @Override
    public List<TaskComment> findByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId)
               .eq(TaskComment::getDeleted, 0)
               .orderByAsc(TaskComment::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public List<TaskComment> findByParentId(Long parentId) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getParentCommentId, parentId)
               .eq(TaskComment::getDeleted, 0)
               .orderByAsc(TaskComment::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public TaskComment findById(Long id) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getId, id)
               .eq(TaskComment::getDeleted, 0);
        return getOne(wrapper);
    }
}
