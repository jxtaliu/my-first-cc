package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.TaskStatus;

import java.util.List;

public interface ITaskStatusService extends IService<TaskStatus> {
    List<TaskStatus> findByProjectId(String projectId);
    List<TaskStatus> findSystemDefaults();
    TaskStatus findByCode(String projectId, String code);

    /**
     * 从业务字典初始化项目任务状态
     * @param projectId 项目ID
     */
    void initializeFromDict(String projectId);

    /**
     * 批量更新任务状态排序
     * @param statusIds 状态ID列表，顺序即为新的排序顺序
     */
    void reorder(List<Long> statusIds);
}
