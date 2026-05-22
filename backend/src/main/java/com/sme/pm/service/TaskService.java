package com.sme.pm.service;

import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;

import java.util.List;

public interface TaskService {
    Task create(Task task);
    Task update(Task task);
    Task move(Task task);
    void delete(Long id);
    Task getById(Long id);
    List<Task> listBySprint(Long sprintId);
    List<Task> listByProject(String projectId);
    List<Task> listByParent(Long parentId);
    List<Task> listByAssignee(Long assigneeId);
    void assign(Long taskId, Long userId);
    Task updateStatus(Long taskId, Long statusId);
    boolean canTransitionTo(Long taskId, Long targetStatusId);
    List<TaskComment> getComments(Long taskId);
    void addComment(Long taskId, TaskComment comment);
    List<TaskAttachment> getAttachments(Long taskId);
    void addAttachment(Long taskId, TaskAttachment attachment);
    void deleteAttachment(Long attachmentId);
    List<TaskDependency> getDependencies(Long taskId);
    List<TaskDependency> getBlockingDependencies(Long taskId);
    void addDependency(Long taskId, TaskDependency dependency);
    void removeDependency(Long dependencyId);
    int countBlockingDependencies(Long taskId);
}
