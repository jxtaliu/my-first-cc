package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.TaskStatus;

import java.util.List;

public interface ITaskStatusService extends IService<TaskStatus> {
    List<TaskStatus> findByProjectId(String projectId);
    List<TaskStatus> findSystemDefaults();
    TaskStatus findByCode(String projectId, String code);
}
