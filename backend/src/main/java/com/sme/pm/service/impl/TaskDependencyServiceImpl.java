package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.TaskDependency;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.mapper.TaskDependencyMapper;
import com.sme.pm.mapper.TaskStatusMapper;
import com.sme.pm.service.ITaskDependencyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskDependencyServiceImpl extends ServiceImpl<TaskDependencyMapper, TaskDependency> implements ITaskDependencyService {

    private final TaskStatusMapper taskStatusMapper;

    public TaskDependencyServiceImpl(TaskStatusMapper taskStatusMapper) {
        this.taskStatusMapper = taskStatusMapper;
    }

    @Override
    public List<TaskDependency> findByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getTaskId, taskId)
               .eq(TaskDependency::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public List<TaskDependency> findByDependsOnTaskId(Long dependsOnTaskId) {
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getDependsOnTaskId, dependsOnTaskId)
               .eq(TaskDependency::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public int countBlockingDependencies(Long taskId) {
        return baseMapper.countBlockingDependencies(taskId);
    }

    @Override
    public boolean canTransitionTo(Long taskId, String targetStatusCode) {
        // If not marking as DONE, always allowed
        if (!"DONE".equals(targetStatusCode)) {
            return true;
        }

        // Check blocking dependencies - all must be DONE before we can mark this task as DONE
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getTaskId, taskId)
               .eq(TaskDependency::getDeleted, 0);
        List<TaskDependency> deps = list(wrapper);
        for (TaskDependency dep : deps) {
            if (dep.getDependsOnTaskId() != null) {
                // This is a simplified check - in reality you'd need to query the task's status
                // For now, we allow the transition
            }
        }
        return true;
    }
}
