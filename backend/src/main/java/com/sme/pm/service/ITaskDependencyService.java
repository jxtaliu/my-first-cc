package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.TaskDependency;

import java.util.List;

public interface ITaskDependencyService extends IService<TaskDependency> {
    List<TaskDependency> findByTaskId(Long taskId);
    List<TaskDependency> findByDependsOnTaskId(Long dependsOnTaskId);
    int countBlockingDependencies(Long taskId);
    boolean canTransitionTo(Long taskId, String targetStatusCode);
}
