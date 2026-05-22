package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.mapper.TaskStatusMapper;
import com.sme.pm.service.ITaskStatusService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusServiceImpl extends ServiceImpl<TaskStatusMapper, TaskStatus> implements ITaskStatusService {

    @Override
    public List<TaskStatus> findByProjectId(Long projectId) {
        LambdaQueryWrapper<TaskStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskStatus::getProjectId, projectId)
               .eq(TaskStatus::getDeleted, 0)
               .orderByAsc(TaskStatus::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<TaskStatus> findSystemDefaults() {
        LambdaQueryWrapper<TaskStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(TaskStatus::getProjectId)
               .eq(TaskStatus::getDeleted, 0)
               .orderByAsc(TaskStatus::getSortOrder);
        return list(wrapper);
    }

    @Override
    public TaskStatus findByCode(Long projectId, String code) {
        LambdaQueryWrapper<TaskStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskStatus::getProjectId, projectId)
               .eq(TaskStatus::getCode, code)
               .eq(TaskStatus::getDeleted, 0);
        return getOne(wrapper);
    }
}
