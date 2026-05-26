package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;

import java.util.List;
import java.util.Map;

public interface ISprintService extends IService<Sprint> {
    List<Sprint> findByProjectId(String projectId);
    Sprint findById(Long id);
    Sprint create(Sprint sprint);
    Sprint update(Sprint sprint);
    void delete(Long id);
    Sprint startSprint(Long id);
    Sprint completeSprint(Long id);

    // Velocity calculation
    int calculateVelocity(Long sprintId);
    Map<String, Object> getSprintStats(Long sprintId);

    // Capacity calculation
    int calculateCapacity(Long sprintId);

    // Sprint planning
    void addTaskToSprint(Long sprintId, Long taskId);
    void removeTaskFromSprint(Long taskId);
    int batchAddTasks(Long sprintId, List<Long> taskIds);
    int batchRemoveTasks(Long sprintId, List<Long> taskIds);
    List<Task> getBacklogTasks(String projectId);
}
