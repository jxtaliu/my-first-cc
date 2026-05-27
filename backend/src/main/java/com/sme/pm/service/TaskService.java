package com.sme.pm.service;

import java.util.List;
import java.util.Map;

import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.entity.TaskDependency;

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
    Task updateStatus(Long taskId, String statusCode);
    boolean canTransitionTo(Long taskId, String targetStatusCode);
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
    int countByStatusId(Integer statusId);

    // Batch operations
    Map<String, Object> batchMove(List<Long> taskIds, String targetStatus, Long sprintId);

    // Requirement tree methods
    List<Task> getRequirementTree(String projectId);
    List<Task> getRequirementChildren(Long parentId);
    List<Task> getRequirementSubtree(Long parentId);
    void moveRequirement(Long taskId, Long newParentId, Integer sortOrder);

    // Bug methods
    List<Task> getBugs(String projectId);
    void updateBugStatus(Long taskId, Long bugStatusId);
}
