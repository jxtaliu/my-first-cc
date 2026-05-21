package com.sme.pm.service;

import com.sme.pm.entity.Task;
import java.util.List;

public interface TaskService {
    Task create(Task task);
    Task update(Task task);
    Task move(Task task);
    void delete(Long id);
    Task getById(Long id);
    List<Task> listBySprint(Long sprintId);
    List<Task> listByProject(Long projectId);
    List<Task> listByParent(Long parentId);
    void assign(Long taskId, Long userId);
    List<Object> getComments(Long taskId);
    void addComment(Long taskId, Object comment);
}
